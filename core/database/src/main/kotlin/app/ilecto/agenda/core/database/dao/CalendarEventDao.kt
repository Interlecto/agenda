package app.ilecto.agenda.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.ilecto.agenda.core.database.entity.CalendarEventEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface CalendarEventDao {
    @Query("SELECT * FROM calendar_events WHERE start < :to AND end > :from ORDER BY start")
    fun observeBetween(from: Instant, to: Instant): Flow<List<CalendarEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(events: List<CalendarEventEntity>)

    @Query("DELETE FROM calendar_events WHERE accountId = :accountId")
    suspend fun deleteForAccount(accountId: String)
}
