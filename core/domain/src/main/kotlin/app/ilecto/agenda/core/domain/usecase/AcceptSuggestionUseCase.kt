package app.ilecto.agenda.core.domain.usecase

import android.content.Context
import app.ilecto.agenda.core.common.AppResult
import app.ilecto.agenda.core.domain.repository.CalendarRepository
import app.ilecto.agenda.core.domain.repository.MailSuggestionRepository
import app.ilecto.agenda.core.model.MailSuggestion
import app.ilecto.agenda.core.model.SuggestionStatus
import javax.inject.Inject

/**
 * Phase 3: creates a real event on the user-chosen [targetAccountId]/[targetCalendarId] for an
 * accepted suggestion, then marks it `ACCEPTED` so it never resurfaces. The target doesn't have
 * to be the suggestion's own source account — any linked calendar is a valid destination.
 */
class AcceptSuggestionUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val mailSuggestionRepository: MailSuggestionRepository,
) {
    suspend operator fun invoke(
        context: Context,
        suggestion: MailSuggestion,
        targetAccountId: String,
        targetCalendarId: String,
    ): AppResult<Unit> {
        val createResult = calendarRepository.createEvent(
            context = context,
            accountId = targetAccountId,
            calendarId = targetCalendarId,
            title = suggestion.title,
            start = suggestion.start,
            end = suggestion.end,
            isAllDay = suggestion.isAllDay,
        )
        if (createResult is AppResult.Error) return createResult
        return mailSuggestionRepository.updateStatus(suggestion.id, SuggestionStatus.ACCEPTED)
    }
}
