package app.ilecto.agenda.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "calendar_events",
    foreignKeys = [
        ForeignKey(
            entity = CalendarAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("accountId"), Index("start"), Index("end")],
)
data class CalendarEventEntity(
    @PrimaryKey val id: String,
    val accountId: String,
    val calendarId: String,
    val title: String,
    val description: String?,
    val location: String?,
    val start: Instant,
    val end: Instant,
    val isAllDay: Boolean,
    val mirroredFromEventId: String?,
    // Phase 2: which SyncRule produced this mirror, so reconciliation scopes itself to one
    // rule's own mirrors even if two rules happen to target the same calendar.
    val mirroredByRuleId: String?,
)
