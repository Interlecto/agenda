package app.ilecto.agenda.core.model

import java.time.Instant

/**
 * A calendar event as shown in the unified view. [mirroredFromEventId] is set when this
 * event was created by the Phase 2 sync engine as a redacted copy of another event — it
 * is the marker used to avoid re-mirroring agenda's own mirrored events.
 */
data class CalendarEvent(
    val id: String,
    val accountId: String,
    val calendarId: String,
    val title: String,
    val description: String?,
    val location: String?,
    val start: Instant,
    val end: Instant,
    val isAllDay: Boolean,
    val mirroredFromEventId: String?,
)
