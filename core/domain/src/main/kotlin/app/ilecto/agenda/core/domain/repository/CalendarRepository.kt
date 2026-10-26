package app.ilecto.agenda.core.domain.repository

import android.content.Context
import app.ilecto.agenda.core.common.AppResult
import app.ilecto.agenda.core.model.CalendarEvent
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Source of truth for calendar events merged across every linked account, backed by the local
 * Room cache and refreshed from :core:network's Google/Outlook clients.
 *
 * [refresh] takes a [Context] because Phase 1 only refreshes from the foreground (pull-to-
 * refresh) — there's no background sync yet (that's Phase 2) — and Google's token path can
 * occasionally need it to resolve a revoked grant.
 */
interface CalendarRepository {
    fun observeEvents(from: Instant, to: Instant): Flow<List<CalendarEvent>>

    suspend fun refresh(context: Context): AppResult<Unit>
}
