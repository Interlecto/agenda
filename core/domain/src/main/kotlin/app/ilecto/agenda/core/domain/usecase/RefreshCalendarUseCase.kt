package app.ilecto.agenda.core.domain.usecase

import android.content.Context
import app.ilecto.agenda.core.common.AppResult
import app.ilecto.agenda.core.domain.repository.CalendarRepository
import javax.inject.Inject

/** Phase 1: the pull-to-refresh action behind the unified calendar view. */
class RefreshCalendarUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository,
) {
    suspend operator fun invoke(context: Context): AppResult<Unit> = calendarRepository.refresh(context)
}
