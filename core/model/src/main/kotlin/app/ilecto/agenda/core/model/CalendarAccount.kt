package app.ilecto.agenda.core.model

/**
 * A single linked calendar account. A user can link more than one account from the
 * same [provider] (e.g. a personal and a corporate Outlook account).
 */
data class CalendarAccount(
    val id: String,
    val provider: CalendarProvider,
    val displayName: String,
    val emailAddress: String,
    val isCorporate: Boolean,
    val colorArgb: Int,
)
