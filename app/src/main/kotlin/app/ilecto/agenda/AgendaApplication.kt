package app.ilecto.agenda

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import app.ilecto.agenda.sync.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class AgendaApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        scheduleBackgroundSync()
    }

    // Phase 2: periodic background sync (refresh + reconcile sync rules). Unconditional —
    // SyncAgendaUseCase itself no-ops cheaply when there are no linked accounts or no enabled
    // rules — and KEEP so re-enqueueing on every app start doesn't reset an already-scheduled
    // job's interval.
    private fun scheduleBackgroundSync() {
        val request = PeriodicWorkRequest.Builder(SyncWorker::class.java, 15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork("agenda-sync", ExistingPeriodicWorkPolicy.KEEP, request)
    }
}
