package app.ilecto.agenda.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sync_rules",
    foreignKeys = [
        ForeignKey(
            entity = CalendarAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceAccountId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CalendarAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["targetAccountId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sourceAccountId"), Index("targetAccountId")],
)
data class SyncRuleEntity(
    @PrimaryKey val id: String,
    val sourceAccountId: String,
    val sourceCalendarId: String,
    val targetAccountId: String,
    val targetCalendarId: String,
    val redactionTemplate: String,
    val keywordTrigger: String?,
    val enabled: Boolean,
)
