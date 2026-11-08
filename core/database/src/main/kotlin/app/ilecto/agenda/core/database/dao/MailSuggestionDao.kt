package app.ilecto.agenda.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.ilecto.agenda.core.database.entity.MailSuggestionEntity
import app.ilecto.agenda.core.model.SuggestionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MailSuggestionDao {
    @Query("SELECT * FROM mail_suggestions WHERE status = :status ORDER BY detectedAt DESC")
    fun observeByStatus(status: SuggestionStatus): Flow<List<MailSuggestionEntity>>

    /** Ids already scanned for this account, so a scan never re-creates a dismissed/accepted row. */
    @Query("SELECT id FROM mail_suggestions WHERE accountId = :accountId")
    suspend fun getExistingIds(accountId: String): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(suggestions: List<MailSuggestionEntity>)

    @Query("UPDATE mail_suggestions SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: SuggestionStatus)
}
