package app.ilecto.agenda.core.domain.repository

import app.ilecto.agenda.core.common.AppResult
import app.ilecto.agenda.core.model.CalendarEvent
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Source of truth for calendar events merged across every linked account. Implemented by
 * :core:data in Phase 1, backed by the local Room cache and refreshed from
 * :core:network's Google/Outlook clients.
 */
interface CalendarRepository {
    fun observeEvents(from: Instant, to: Instant): Flow<List<CalendarEvent>>

    suspend fun refresh(): AppResult<Unit>
}
