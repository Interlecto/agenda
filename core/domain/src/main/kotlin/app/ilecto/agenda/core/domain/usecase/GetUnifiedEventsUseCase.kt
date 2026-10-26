package app.ilecto.agenda.core.domain.usecase

import app.ilecto.agenda.core.domain.repository.CalendarRepository
import app.ilecto.agenda.core.model.CalendarEvent
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject

/** Phase 1: the merged, sorted event list behind the unified calendar view. */
class GetUnifiedEventsUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository,
) {
    operator fun invoke(from: Instant, to: Instant): Flow<List<CalendarEvent>> =
        calendarRepository.observeEvents(from, to)
}
