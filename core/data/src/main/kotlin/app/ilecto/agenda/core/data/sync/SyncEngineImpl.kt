package app.ilecto.agenda.core.data.sync

import android.content.Context
import app.ilecto.agenda.core.common.AppResult
import app.ilecto.agenda.core.data.mapper.buildGoogleMirrorWrite
import app.ilecto.agenda.core.data.mapper.buildGraphMirrorWrite
import app.ilecto.agenda.core.database.dao.CalendarAccountDao
import app.ilecto.agenda.core.database.dao.CalendarEventDao
import app.ilecto.agenda.core.database.dao.SyncRuleDao
import app.ilecto.agenda.core.database.entity.CalendarAccountEntity
import app.ilecto.agenda.core.database.entity.CalendarEventEntity
import app.ilecto.agenda.core.database.entity.SyncRuleEntity
import app.ilecto.agenda.core.domain.sync.SyncEngine
import app.ilecto.agenda.core.model.CalendarProvider
import app.ilecto.agenda.core.network.auth.TokenProviderFactory
import app.ilecto.agenda.core.network.google.GoogleCalendarApi
import app.ilecto.agenda.core.network.outlook.OutlookGraphApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Phase 2: reconciles every enabled [SyncRuleEntity] against the local cache a prior
 * [CalendarRepository.refresh][app.ilecto.agenda.core.domain.repository.CalendarRepository.refresh]
 * already populated — it never fetches *source* events itself, only ever writes to the
 * *target* calendar. Idempotent: a rule with nothing to change issues no network calls.
 *
 * The only place source events are read is [CalendarEventDao.getSourceCandidates], which
 * excludes the app's own mirrored events at the query level — this is what stops a mirror from
 * ever being treated as a source for another rule.
 */
@Singleton
class SyncEngineImpl @Inject constructor(
    private val syncRuleDao: SyncRuleDao,
    private val accountDao: CalendarAccountDao,
    private val eventDao: CalendarEventDao,
    private val tokenProviderFactory: TokenProviderFactory,
    private val googleCalendarApi: GoogleCalendarApi,
    private val outlookGraphApi: OutlookGraphApi,
) : SyncEngine {
    override suspend fun reconcileAll(context: Context): AppResult<Unit> {
        val rules = syncRuleDao.getEnabled()
        if (rules.isEmpty()) return AppResult.Success(Unit)

        val accountsById = accountDao.observeAll().first().associateBy { it.id }
        var failure: Throwable? = null

        for (rule in rules) {
            try {
                reconcileRule(context, rule, accountsById)
            } catch (e: Exception) {
                // Keep going: one broken rule (revoked target token, deleted calendar, ...)
                // shouldn't block the others from reconciling.
                failure = e
            }
        }

        return failure?.let { AppResult.Error(it) } ?: AppResult.Success(Unit)
    }

    private suspend fun reconcileRule(
        context: Context,
        rule: SyncRuleEntity,
        accountsById: Map<String, CalendarAccountEntity>,
    ) {
        val targetAccount = accountsById[rule.targetAccountId] ?: return // target was unlinked

        val sourceCandidates = eventDao.getSourceCandidates(rule.sourceAccountId, rule.sourceCalendarId)
        val desiredBySourceKey = sourceCandidates
            .filter { matchesTrigger(it, rule.keywordTrigger) }
            .associateBy { sourceKey(rule.sourceAccountId, it.id) }

        val existingBySourceKey = eventDao.getMirrorsForTarget(rule.targetAccountId, rule.targetCalendarId)
            .filter { it.mirroredByRuleId == rule.id }
            .mapNotNull { mirror -> mirror.mirroredFromEventId?.let { it to mirror } }
            .toMap()

        // Nothing to create/update/delete for this rule right now — skip acquiring a token.
        if (desiredBySourceKey.isEmpty() && existingBySourceKey.isEmpty()) return

        val tokenResult = tokenProviderFactory.forProvider(targetAccount.provider)
            .getAccessToken(context, targetAccount.authRef)
        val authorization = when (tokenResult) {
            is AppResult.Success -> "Bearer ${tokenResult.data}"
            is AppResult.Error -> throw tokenResult.throwable
        }

        for ((key, mirror) in existingBySourceKey) {
            if (key !in desiredBySourceKey) {
                deleteMirror(authorization, targetAccount, rule.targetCalendarId, mirror)
            }
        }

        for ((key, source) in desiredBySourceKey) {
            val existing = existingBySourceKey[key]
            when {
                existing == null -> createMirror(authorization, targetAccount, rule, source, key)
                existing.start != source.start || existing.end != source.end || existing.title != rule.redactionTemplate ->
                    updateMirror(authorization, targetAccount, rule, source, existing, key)
            }
        }
    }

    private fun matchesTrigger(event: CalendarEventEntity, keyword: String?): Boolean {
        if (keyword.isNullOrBlank()) return true
        return event.title.contains(keyword, ignoreCase = true) ||
            event.description?.contains(keyword, ignoreCase = true) == true
    }

    private fun sourceKey(sourceAccountId: String, sourceEventId: String) = "$sourceAccountId:$sourceEventId"

    private suspend fun createMirror(
        authorization: String,
        targetAccount: CalendarAccountEntity,
        rule: SyncRuleEntity,
        source: CalendarEventEntity,
        sourceKeyValue: String,
    ) {
        val remoteId = when (targetAccount.provider) {
            CalendarProvider.GOOGLE -> googleCalendarApi.insertEvent(
                authorization = authorization,
                calendarId = rule.targetCalendarId,
                event = buildGoogleMirrorWrite(
                    rule.redactionTemplate,
                    source.start,
                    source.end,
                    sourceKeyValue,
                    rule.id,
                ),
            ).id
            CalendarProvider.OUTLOOK -> outlookGraphApi.createEvent(
                authorization = authorization,
                calendarId = rule.targetCalendarId,
                event = buildGraphMirrorWrite(
                    rule.redactionTemplate,
                    source.start,
                    source.end,
                    sourceKeyValue,
                    rule.id,
                ),
            ).id
        }
        eventDao.upsertAll(
            listOf(
                CalendarEventEntity(
                    id = remoteId,
                    accountId = targetAccount.id,
                    calendarId = rule.targetCalendarId,
                    title = rule.redactionTemplate,
                    description = null,
                    location = null,
                    start = source.start,
                    end = source.end,
                    isAllDay = source.isAllDay,
                    mirroredFromEventId = sourceKeyValue,
                    mirroredByRuleId = rule.id,
                ),
            ),
        )
    }

    private suspend fun updateMirror(
        authorization: String,
        targetAccount: CalendarAccountEntity,
        rule: SyncRuleEntity,
        source: CalendarEventEntity,
        existing: CalendarEventEntity,
        sourceKeyValue: String,
    ) {
        when (targetAccount.provider) {
            CalendarProvider.GOOGLE -> googleCalendarApi.patchEvent(
                authorization = authorization,
                calendarId = rule.targetCalendarId,
                eventId = existing.id,
                event = buildGoogleMirrorWrite(
                    rule.redactionTemplate,
                    source.start,
                    source.end,
                    sourceKeyValue,
                    rule.id,
                ),
            )
            CalendarProvider.OUTLOOK -> outlookGraphApi.updateEvent(
                authorization = authorization,
                calendarId = rule.targetCalendarId,
                eventId = existing.id,
                event = buildGraphMirrorWrite(
                    rule.redactionTemplate,
                    source.start,
                    source.end,
                    sourceKeyValue,
                    rule.id,
                ),
            )
        }
        eventDao.upsertAll(
            listOf(
                existing.copy(
                    title = rule.redactionTemplate,
                    start = source.start,
                    end = source.end,
                    isAllDay = source.isAllDay,
                ),
            ),
        )
    }

    private suspend fun deleteMirror(
        authorization: String,
        targetAccount: CalendarAccountEntity,
        targetCalendarId: String,
        mirror: CalendarEventEntity,
    ) {
        val response = when (targetAccount.provider) {
            CalendarProvider.GOOGLE -> googleCalendarApi.deleteEvent(authorization, targetCalendarId, mirror.id)
            CalendarProvider.OUTLOOK -> outlookGraphApi.deleteEvent(authorization, targetCalendarId, mirror.id)
        }
        // A 404 here means the mirror was already removed remotely (e.g. the user deleted it by
        // hand) — treat that as success too, only a genuine failure (permission revoked, etc.)
        // should stop the local cleanup and bubble up to the per-rule catch in reconcileAll.
        check(response.isSuccessful || response.code() == 404) {
            "Mirror delete failed: HTTP ${response.code()}"
        }
        eventDao.deleteById(mirror.id)
    }
}
