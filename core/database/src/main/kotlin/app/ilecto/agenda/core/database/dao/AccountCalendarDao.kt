package app.ilecto.agenda.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.ilecto.agenda.core.database.entity.AccountCalendarEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountCalendarDao {
    @Query("SELECT * FROM account_calendars WHERE accountId = :accountId")
    fun observeForAccount(accountId: String): Flow<List<AccountCalendarEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(calendars: List<AccountCalendarEntity>)

    @Query("DELETE FROM account_calendars WHERE accountId = :accountId")
    suspend fun deleteForAccount(accountId: String)
}
