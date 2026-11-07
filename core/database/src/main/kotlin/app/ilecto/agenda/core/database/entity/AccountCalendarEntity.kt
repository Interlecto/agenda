package app.ilecto.agenda.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A calendar belonging to a linked account (e.g. a Google account's "Personal" and "Birthdays"
 * calendars, or an Outlook account's default calendar). Cached from each refresh's
 * `listCalendars()` call purely so Phase 2's sync-rule editor can show real calendar names
 * instead of raw provider ids.
 */
@Entity(
    tableName = "account_calendars",
    primaryKeys = ["accountId", "calendarId"],
    foreignKeys = [
        ForeignKey(
            entity = CalendarAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("accountId")],
)
data class AccountCalendarEntity(
    val accountId: String,
    val calendarId: String,
    val displayName: String,
)
