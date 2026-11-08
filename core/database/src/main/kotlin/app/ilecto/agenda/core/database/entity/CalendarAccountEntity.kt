package app.ilecto.agenda.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.ilecto.agenda.core.model.CalendarProvider

@Entity(tableName = "calendar_accounts")
data class CalendarAccountEntity(
    @PrimaryKey val id: String,
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
