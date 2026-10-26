package app.ilecto.agenda.core.data.repository

import android.content.Context
import app.ilecto.agenda.core.common.AppResult
import app.ilecto.agenda.core.data.mapper.toDomain
import app.ilecto.agenda.core.data.mapper.toEntity
import app.ilecto.agenda.core.database.dao.CalendarAccountDao
import app.ilecto.agenda.core.database.dao.CalendarEventDao
import app.ilecto.agenda.core.database.entity.CalendarAccountEntity
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

        val events = when (accountEntity.provider) {
            CalendarProvider.GOOGLE -> fetchGoogleEvents(authorization, accountEntity.id, from, to)
            CalendarProvider.OUTLOOK -> fetchOutlookEvents(authorization, accountEntity.id, from, to)
        }

        eventDao.deleteForAccount(accountEntity.id)
        eventDao.upsertAll(events)
    }

    private suspend fun fetchGoogleEvents(
        authorization: String,
        accountId: String,
        from: Instant,
        to: Instant,
    ) = googleCalendarApi.listCalendars(authorization).items.flatMap { calendar ->
        googleCalendarApi.listEvents(
            authorization = authorization,
            calendarId = calendar.id,
            timeMin = from.toString(),
            timeMax = to.toString(),
        ).items.mapNotNull { it.toEntity(accountId = accountId, calendarId = calendar.id) }
    }

    private suspend fun fetchOutlookEvents(
        authorization: String,
        accountId: String,
        from: Instant,
        to: Instant,
    ) = outlookGraphApi.listCalendars(authorization).value.flatMap { calendar ->
        outlookGraphApi.listEvents(
            authorization = authorization,
            calendarId = calendar.id,
            startDateTime = GRAPH_DATE_TIME_FORMAT.format(from),
            endDateTime = GRAPH_DATE_TIME_FORMAT.format(to),
        ).value.mapNotNull { it.toEntity(accountId = accountId, calendarId = calendar.id) }
    }

    companion object {
        private val GRAPH_DATE_TIME_FORMAT = DateTimeFormatter.ISO_INSTANT
    }
}
