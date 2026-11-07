package app.ilecto.agenda.core.domain.sync

import android.content.Context
import app.ilecto.agenda.core.common.AppResult

/**
 * Reconciles every enabled sync rule against the local calendar cache, mirroring matching
 * source events onto each rule's target calendar as redacted "busy" blocks — see the `core:data`
 * implementation for the full algorithm. Takes a [Context] for the same reason
 * [app.ilecto.agenda.core.domain.repository.CalendarRepository.refresh] does: Phase 2 only runs
 * this from the foreground or a WorkManager job, never while interactive UI is required.
 */
interface SyncEngine {
    suspend fun reconcileAll(context: Context): AppResult<Unit>
}
