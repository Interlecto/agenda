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
    // Google: the signed-in account's email, used to re-request the Calendar scope later.
    // Outlook: MSAL's homeAccountId, used with acquireTokenSilent/getAccount later.
    val authRef: String,
    // Phase 3: whether the user has granted the separate mail-readonly scope and opted into
    // mail scanning for this account.
    val mailScanEnabled: Boolean,
)
