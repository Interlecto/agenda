package app.ilecto.agenda.core.data.repository

import app.ilecto.agenda.core.common.AppResult
import app.ilecto.agenda.core.data.mapper.toDomain
import app.ilecto.agenda.core.database.dao.MailSuggestionDao
import app.ilecto.agenda.core.domain.repository.MailSuggestionRepository
import app.ilecto.agenda.core.model.MailSuggestion
import app.ilecto.agenda.core.model.SuggestionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MailSuggestionRepositoryImpl @Inject constructor(
    private val mailSuggestionDao: MailSuggestionDao,
) : MailSuggestionRepository {

    override fun observeByStatus(status: SuggestionStatus): Flow<List<MailSuggestion>> =
        mailSuggestionDao.observeByStatus(status).map { entities -> entities.map { it.toDomain() } }

    override suspend fun updateStatus(suggestionId: String, status: SuggestionStatus): AppResult<Unit> = try {
        mailSuggestionDao.updateStatus(suggestionId, status)
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e)
    }
}
