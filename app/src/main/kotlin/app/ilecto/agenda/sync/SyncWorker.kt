package app.ilecto.agenda.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.ilecto.agenda.core.common.AppResult
import app.ilecto.agenda.core.domain.usecase.SyncAgendaUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Phase 2's background sync pass (WorkManager's enforced floor is 15 minutes — this is not
 * real-time). Never fails hard: an aggregated error asks WorkManager to retry later rather than
 * crashing, since a single broken account/rule already doesn't block the others inside
 * [SyncAgendaUseCase] itself.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncAgendaUseCase: SyncAgendaUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = when (syncAgendaUseCase(applicationContext)) {
        is AppResult.Success -> Result.success()
        is AppResult.Error -> Result.retry()
    }
}
