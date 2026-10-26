package app.ilecto.agenda.core.model

/** Phase 4: a container for related [Task]s. */
data class Project(
    val id: String,
    val name: String,
    val colorArgb: Int,
    val archived: Boolean,
)
