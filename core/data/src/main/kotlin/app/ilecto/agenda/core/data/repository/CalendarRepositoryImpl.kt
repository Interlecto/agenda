package app.ilecto.agenda.core.data.repository

import android.content.Context
import app.ilecto.agenda.core.common.AppResult
import app.ilecto.agenda.core.data.mapper.buildGooglePlainWrite
import app.ilecto.agenda.core.data.mapper.buildGraphPlainWrite
import app.ilecto.agenda.core.data.mapper.toDomain
import app.ilecto.agenda.core.data.mapper.toEntity
import app.ilecto.agenda.core.database.dao.AccountCalendarDao
import app.ilecto.agenda.core.database.dao.CalendarAccountDao
import app.ilecto.agenda.core.database.dao.CalendarEventDao
import app.ilecto.agenda.core.database.entity.AccountCalendarEntity
import app.ilecto.agenda.core.database.entity.CalendarAccountEntity
import app.ilecto.agenda.core.database.entity.CalendarEventEntity
import app.ilecto.agenda.core.domain.repository.CalendarRepository
import app.ilecto.agenda.core.model.CalendarEvent
import app.ilecto.agenda.core.model.CalendarProvider
import app.ilecto.agenda.core.network.auth.TokenProviderFactory
import app.ilecto.agenda.core.network.google.GoogleCalendarApi
import app.ilecto.agenda.core.network.outlook.OutlookGraphApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/** How far ahead each refresh fetches — Phase 1 has no delta/sync-token support yet. */
private val REFRESH_WINDOW = Duration.ofDays(30)

class CalendarRepositoryImpl @Inject constructor(
    private val accountDao: CalendarAccountDao,
    private val eventDao: CalendarEventDao,
    private val accountCalendarDao: AccountCalendarDao,
    private val tokenProviderFactory: TokenProviderFactory,
    private val googleCalendarApi: GoogleCalendarApi,
    private val outlookGraphApi: OutlookGraphApi,
) : CalendarRepository {

    override fun observeEvents(from: Instant, to: Instant): Flow<List<CalendarEvent>> =
        eventDao.observeBetween(from, to).map { entities -> entities.map { it.toDomain() } }

    override suspend fun refresh(context: Context): AppResult<Unit> {
        val accounts = accountDao.observeAll().first()
        if (accounts.isEmpty()) return AppResult.Success(Unit)

        val now = Instant.now()
        val windowEnd = now.plus(REFRESH_WINDOW)
        var failure: Throwable? = null

        for (accountEntity in accounts) {
            try {
                refreshAccount(context, accountEntity, now, windowEnd)
            } catch (e: Exception) {
                // Keep going: one broken account shouldn't block the others from refreshing.
                failure = e
            }
        }

        return failure?.let { AppResult.Error(it) } ?: AppResult.Success(Unit)
    }

    override suspend fun createEvent(
        context: Context,
        accountId: String,
        calendarId: String,
        title: String,
        start: Instant,
        end: Instant,
        isAllDay: Boolean,
    ): AppResult<Unit> {
        val accountEntity = accountDao.observeAll().first().find { it.id == accountId }
            ?: return AppResult.Error(IllegalStateException("Account $accountId is no longer linked"))
        val tokenResult = tokenProviderFactory.forProvider(accountEntity.provider)
            .getAccessToken(context, accountEntity.authRef)
        val token = when (tokenResult) {
            is AppResult.Success -> tokenResult.data
            is AppResult.Error -> return AppResult.Error(tokenResult.throwable)
        }
        val authorization = "Bearer $token"

        return try {
            val remoteId = when (accountEntity.provider) {
                CalendarProvider.GOOGLE -> googleCalendarApi.insertEvent(
                    authorization = authorization,
                    calendarId = calendarId,
                    event = buildGooglePlainWrite(title, start, end),
                ).id
                CalendarProvider.OUTLOOK -> outlookGraphApi.createEvent(
                    authorization = authorization,
                    calendarId = calendarId,
                    event = buildGraphPlainWrite(title, start, end),
                ).id
            }
            eventDao.upsertAll(
                listOf(
                    CalendarEventEntity(
                        id = remoteId,
                        accountId = accountId,
                        calendarId = calendarId,
                        title = title,
                        description = null,
                        location = null,
                        start = start,
                        end = end,
                        isAllDay = isAllDay,
                        mirroredFromEventId = null,
                        mirroredByRuleId = null,
                    ),
                ),
            )
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    private suspend fun refreshAccount(
        context: Context,
        accountEntity: CalendarAccountEntity,
        from: Instant,
        to: Instant,
    ) {
        val tokenResult = tokenProviderFactory.forProvider(accountEntity.provider)
            .getAccessToken(context, accountEntity.authRef)
        val token = when (tokenResult) {
            is AppResult.Success -> tokenResult.data
            is AppResult.Error -> throw tokenResult.throwable
        }
        val authorization = "Bearer $token"

        val (calendars, events) = when (accountEntity.provider) {
            CalendarProvider.GOOGLE -> fetchGoogle(authorization, accountEntity.id, from, to)
            CalendarProvider.OUTLOOK -> fetchOutlook(authorization, accountEntity.id, from, to)
        }

        // Phase 2: also cache the account's own calendar list (previously fetched and
        // discarded) so the sync-rule editor can show real calendar names.
        accountCalendarDao.deleteForAccount(accountEntity.id)
        accountCalendarDao.upsertAll(calendars)
        eventDao.deleteForAccount(accountEntity.id)
        eventDao.upsertAll(events)
    }

    private suspend fun fetchGoogle(
        authorization: String,
        accountId: String,
        from: Instant,
        to: Instant,
    ): Pair<List<AccountCalendarEntity>, List<CalendarEventEntity>> {
        val calendarList = googleCalendarApi.listCalendars(authorization).items
        val calendars = calendarList.map {
            AccountCalendarEntity(accountId = accountId, calendarId = it.id, displayName = it.summary ?: it.id)
        }
        val events = calendarList.flatMap { calendar ->
            googleCalendarApi.listEvents(
                authorization = authorization,
                calendarId = calendar.id,
                timeMin = from.toString(),
                timeMax = to.toString(),
            ).items.mapNotNull { it.toEntity(accountId = accountId, calendarId = calendar.id) }
        }
        return calendars to events
    }

    private suspend fun fetchOutlook(
        authorization: String,
        accountId: String,
        from: Instant,
        to: Instant,
    ): Pair<List<AccountCalendarEntity>, List<CalendarEventEntity>> {
        val calendarList = outlookGraphApi.listCalendars(authorization).value
        val calendars = calendarList.map {
            AccountCalendarEntity(accountId = accountId, calendarId = it.id, displayName = it.name ?: it.id)
        }
        val events = calendarList.flatMap { calendar ->
            outlookGraphApi.listEvents(
                authorization = authorization,
                calendarId = calendar.id,
                startDateTime = GRAPH_DATE_TIME_FORMAT.format(from),
                endDateTime = GRAPH_DATE_TIME_FORMAT.format(to),
            ).value.mapNotNull { it.toEntity(accountId = accountId, calendarId = calendar.id) }
        }
        return calendars to events
    }

    companion object {
        private val GRAPH_DATE_TIME_FORMAT = DateTimeFormatter.ISO_INSTANT
    }
}
