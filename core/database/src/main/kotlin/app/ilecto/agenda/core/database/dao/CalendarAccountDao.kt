package app.ilecto.agenda.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.ilecto.agenda.core.database.entity.CalendarAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarAccountDao {
    @Query("SELECT * FROM calendar_accounts")
    fun observeAll(): Flow<List<CalendarAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(account: CalendarAccountEntity)

    @Delete
    suspend fun delete(account: CalendarAccountEntity)

    @Query("DELETE FROM calendar_accounts WHERE id = :accountId")
    suspend fun deleteById(accountId: String)
}
