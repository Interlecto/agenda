package app.ilecto.agenda.core.domain.repository

import app.ilecto.agenda.core.common.AppResult
import app.ilecto.agenda.core.model.MailSuggestion
import app.ilecto.agenda.core.model.SuggestionStatus
import kotlinx.coroutines.flow.Flow

/** The Phase 3 mail-suggestion review inbox — populated by [app.ilecto.agenda.core.domain.mail.MailScanEngine]. */
interface MailSuggestionRepository {
    fun observeByStatus(status: SuggestionStatus): Flow<List<MailSuggestion>>

    suspend fun updateStatus(suggestionId: String, status: SuggestionStatus): AppResult<Unit>
}
