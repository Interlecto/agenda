package app.ilecto.agenda.core.domain.usecase

import android.content.Context
import app.ilecto.agenda.core.common.AppResult
import app.ilecto.agenda.core.domain.repository.CalendarRepository
import app.ilecto.agenda.core.domain.sync.SyncEngine
import javax.inject.Inject

/**
 * Phase 2: a full sync pass — refresh every linked account from its provider, then reconcile
 * every enabled sync rule against the freshly refreshed cache. Used by both feature:calendar's
 * pull-to-refresh action and the background WorkManager job, so there's a single code path for
 * "do a full sync pass."
 */
class SyncAgendaUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val syncEngine: SyncEngine,
) {
    suspend operator fun invoke(context: Context): AppResult<Unit> {
        val refreshResult = calendarRepository.refresh(context)
        val syncResult = syncEngine.reconcileAll(context)
        return when {
            refreshResult is AppResult.Error -> refreshResult
            syncResult is AppResult.Error -> syncResult
            else -> AppResult.Success(Unit)
        }
    }
}
