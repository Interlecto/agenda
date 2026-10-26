package app.ilecto.agenda.core.model

import java.time.Instant

enum class ActivityLogSource { CALENDAR_EVENT, TASK, MANUAL }

/** Phase 5: a record of something the user actually did, for the Phase 6 reports. */
data class ActivityLogEntry(
    val id: String,
    val occurredAt: Instant,
    val description: String,
    val source: ActivityLogSource,
    val sourceRefId: String?,
)
