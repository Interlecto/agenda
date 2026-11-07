package app.ilecto.agenda.core.domain.repository

import app.ilecto.agenda.core.common.AppResult
import app.ilecto.agenda.core.model.SyncRule
import kotlinx.coroutines.flow.Flow

/** Manages the Phase 2 cross-calendar blocking rules (feature:sync's rule editor). */
interface SyncRuleRepository {
    fun observeRules(): Flow<List<SyncRule>>

    suspend fun saveRule(rule: SyncRule): AppResult<Unit>

    suspend fun deleteRule(ruleId: String): AppResult<Unit>
}
