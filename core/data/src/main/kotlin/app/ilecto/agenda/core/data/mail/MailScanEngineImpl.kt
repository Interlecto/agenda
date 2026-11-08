package app.ilecto.agenda.core.data.mail

import android.content.Context
import app.ilecto.agenda.core.common.AppResult
import app.ilecto.agenda.core.data.mapper.toSuggestionOrNull
import app.ilecto.agenda.core.database.dao.CalendarAccountDao
import app.ilecto.agenda.core.database.dao.MailSuggestionDao
import app.ilecto.agenda.core.database.entity.CalendarAccountEntity
import app.ilecto.agenda.core.database.entity.MailSuggestionEntity
import app.ilecto.agenda.core.domain.mail.MailScanEngine
import app.ilecto.agenda.core.model.CalendarProvider
import app.ilecto.agenda.core.network.auth.GoogleAuthManager
import app.ilecto.agenda.core.network.auth.OutlookAuthManager
import app.ilecto.agenda.core.network.google.GmailApi
import app.ilecto.agenda.core.network.outlook.OutlookGraphApi
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/** How far back each scan looks — capped deliberately narrow, see the Phase 3 plan notes. */
private val SCAN_WINDOW = Duration.ofDays(2)
private const val MAX_MESSAGES_PER_SCAN = 25

@Singleton
class MailScanEngineImpl @Inject constructor(
    private val accountDao: CalendarAccountDao,
    private val mailSuggestionDao: MailSuggestionDao,
    private val googleAuthManager: GoogleAuthManager,
    private val outlookAuthManager: OutlookAuthManager,
    private val gmailApi: GmailApi,
    private val outlookGraphApi: OutlookGraphApi,
) : MailScanEngine {

    override suspend fun scanAll(context: Context): AppResult<Unit> {
        val accounts = accountDao.observeAll().first().filter { it.mailScanEnabled }
        if (accounts.isEmpty()) return AppResult.Success(Unit)

        var failure: Throwable? = null
        for (account in accounts) {
            try {
                scanAccount(context, account)
            } catch (e: Exception) {
                // Keep going: one broken account (revoked mail grant, ...) shouldn't block the
                // others from scanning.
                failure = e
            }
        }
        return failure?.let { AppResult.Error(it) } ?: AppResult.Success(Unit)
    }

    private suspend fun scanAccount(context: Context, account: CalendarAccountEntity) {
        val now = Instant.now()
        val existingIds = mailSuggestionDao.getExistingIds(account.id).toHashSet()

        val suggestions = when (account.provider) {
            CalendarProvider.GOOGLE -> scanGmail(context, account, now, existingIds)
            CalendarProvider.OUTLOOK -> scanOutlookMail(context, account, now, existingIds)
        }
        if (suggestions.isNotEmpty()) mailSuggestionDao.insertAll(suggestions)
    }

    private suspend fun scanGmail(
        context: Context,
        account: CalendarAccountEntity,
        now: Instant,
        existingIds: Set<String>,
    ): List<MailSuggestionEntity> {
        // Background-only: no UI available here, matching GoogleTokenProvider's calendar-token
        // path — if the mail grant was revoked, this surfaces as a per-account scan failure
        // rather than trying to launch a consent screen from a worker.
        val tokenResult = googleAuthManager.requestMailAccess(context) { null }
        val token = when (tokenResult) {
            is AppResult.Success -> tokenResult.data
            is AppResult.Error -> throw tokenResult.throwable
        }
        val authorization = "Bearer $token"

        val refs = gmailApi.listMessages(
            authorization = authorization,
            q = "newer_than:${SCAN_WINDOW.toDays()}d",
            maxResults = MAX_MESSAGES_PER_SCAN,
        ).messages
        return refs.mapNotNull { ref ->
            val suggestionId = "${account.id}:${ref.id}"
            if (suggestionId in existingIds) return@mapNotNull null
            gmailApi.getMessage(authorization, ref.id).toSuggestionOrNull(account.id, now)
        }
    }

    private suspend fun scanOutlookMail(
        context: Context,
        account: CalendarAccountEntity,
        now: Instant,
        existingIds: Set<String>,
    ): List<MailSuggestionEntity> {
        val tokenResult = outlookAuthManager.acquireMailTokenSilent(account.authRef)
        val token = when (tokenResult) {
            is AppResult.Success -> tokenResult.data
            is AppResult.Error -> throw tokenResult.throwable
        }
        val authorization = "Bearer $token"

        val since = Instant.now().minus(SCAN_WINDOW)
        val messages = outlookGraphApi.listMessages(
            authorization = authorization,
            filter = "receivedDateTime ge $since",
            top = MAX_MESSAGES_PER_SCAN,
        ).value
        return messages.mapNotNull { message ->
            val suggestionId = "${account.id}:${message.id}"
            if (suggestionId in existingIds) return@mapNotNull null
            message.toSuggestionOrNull(account.id, now)
        }
    }
}
