package app.ilecto.agenda.core.domain.mail

import android.content.Context
import app.ilecto.agenda.core.common.AppResult

/**
 * Phase 3: scans every account with mail scanning enabled for candidate appointment/deadline
 * emails and records new matches as `PENDING` suggestions — see the `core:data` implementation
 * for the full heuristic. Never writes to a calendar itself; that only happens when the user
 * explicitly accepts a suggestion (`AcceptSuggestionUseCase`). Takes a [Context] for the same
 * reason [app.ilecto.agenda.core.domain.sync.SyncEngine.reconcileAll] does: this only runs from
 * the foreground or a WorkManager job, never while interactive UI is required.
 */
interface MailScanEngine {
    suspend fun scanAll(context: Context): AppResult<Unit>
}
