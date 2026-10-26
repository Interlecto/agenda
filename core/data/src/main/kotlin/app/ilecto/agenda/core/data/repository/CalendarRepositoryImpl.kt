package app.ilecto.agenda.core.data.repository

import app.ilecto.agenda.core.common.AppResult
import app.ilecto.agenda.core.data.mapper.toDomain
import app.ilecto.agenda.core.database.dao.CalendarEventDao
import app.ilecto.agenda.core.domain.repository.CalendarRepository
import app.ilecto.agenda.core.model.CalendarEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class CalendarRepositoryImpl @Inject constructor(
    private val eventDao: CalendarEventDao,
) : CalendarRepository {

    override fun observeEvents(from: Instant, to: Instant): Flow<List<CalendarEvent>> =
        eventDao.observeBetween(from, to).map { entities -> entities.map { it.toDomain() } }

    override suspend fun refresh(): AppResult<Unit> {
        // Phase 1 TODO: fetch events from GoogleCalendarApi/OutlookGraphApi for every
        // linked account and upsert them via eventDao. No accounts can be linked yet,
        // so this is intentionally a no-op rather than a fake success.
        return AppResult.Success(Unit)
    }
}
