package app.ilecto.agenda.core.model

import java.time.Instant

enum class TaskPriority { LOW, MEDIUM, HIGH }

/**
 * Phase 4: a to-do item, optionally belonging to a [Project] and optionally pushed
 * onto the unified calendar when it has a [dueAt].
 */
data class Task(
    val id: String,
    val projectId: String?,
    val title: String,
    val notes: String?,
    val dueAt: Instant?,
    val priority: TaskPriority,
    val completedAt: Instant?,
    val pushedToCalendarEventId: String?,
)
