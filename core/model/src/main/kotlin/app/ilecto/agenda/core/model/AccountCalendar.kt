package app.ilecto.agenda.core.model

/**
 * A calendar belonging to a linked account (e.g. a Google account's "Personal" and "Birthdays"
 * calendars). Phase 2's sync-rule editor uses this to let the user pick real calendar names
 * instead of raw provider ids.
 */
data class AccountCalendar(
    val accountId: String,
    val calendarId: String,
    val displayName: String,
)
