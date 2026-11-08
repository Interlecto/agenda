package app.ilecto.agenda.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import app.ilecto.agenda.core.model.SuggestionStatus
import java.time.Instant

@Entity(
    tableName = "mail_suggestions",
    foreignKeys = [
        ForeignKey(
            entity = CalendarAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("accountId"), Index("status")],
)
data class MailSuggestionEntity(
    @PrimaryKey val id: String,
    val accountId: String,
    val messageId: String,
    val title: String,
    val start: Instant,
    val end: Instant,
    val isAllDay: Boolean,
    val sourceSnippet: String,
    val status: SuggestionStatus,
    val detectedAt: Instant,
)
