package app.ilecto.agenda.core.data.repository

import app.ilecto.agenda.core.common.AppResult
import app.ilecto.agenda.core.data.mapper.toDomain
import app.ilecto.agenda.core.data.mapper.toEntity
import app.ilecto.agenda.core.database.dao.SyncRuleDao
import app.ilecto.agenda.core.domain.repository.SyncRuleRepository
import app.ilecto.agenda.core.model.SyncRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SyncRuleRepositoryImpl @Inject constructor(
    private val syncRuleDao: SyncRuleDao,
) : SyncRuleRepository {

    override fun observeRules(): Flow<List<SyncRule>> =
        syncRuleDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun saveRule(rule: SyncRule): AppResult<Unit> = try {
        syncRuleDao.upsert(rule.toEntity())
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e)
    }

    override suspend fun deleteRule(ruleId: String): AppResult<Unit> = try {
        syncRuleDao.deleteById(ruleId)
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e)
    }
}
