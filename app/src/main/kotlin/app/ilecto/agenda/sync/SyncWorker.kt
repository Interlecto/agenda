package app.ilecto.agenda.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.ilecto.agenda.core.common.AppResult
import app.ilecto.agenda.core.domain.usecase.ScanMailUseCase
import app.ilecto.agenda.core.domain.usecase.SyncAgendaUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Phase 2/3's background pass (WorkManager's enforced floor is 15 minutes — this is not
 * real-time): calendar refresh + sync-rule reconciliation, then a mail scan. Never fails hard:
 * an aggregated error asks WorkManager to retry later rather than crashing, since a single
 * broken account/rule/message already doesn't block the others inside each use case itself.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncAgendaUseCase: SyncAgendaUseCase,
    private val scanMailUseCase: ScanMailUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val syncResult = syncAgendaUseCase(applicationContext)
        val mailResult = scanMailUseCase(applicationContext)
        return if (syncResult is AppResult.Error || mailResult is AppResult.Error) {
            Result.retry()
        } else {
            Result.success()
        }
    }
}
