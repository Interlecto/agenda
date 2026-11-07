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

    @Query("DELETE FROM calendar_events WHERE id = :eventId")
    suspend fun deleteById(eventId: String)

    /**
     * Events a sync rule may treat as a *source*. Always excludes the app's own mirrored events
     * ([CalendarEventEntity.mirroredFromEventId] not null) — this is the one place that
     * invariant is enforced, so an event Agenda itself created can never be re-mirrored
     * elsewhere.
     */
    @Query(
        "SELECT * FROM calendar_events WHERE accountId = :accountId AND calendarId = :calendarId " +
            "AND mirroredFromEventId IS NULL",
    )
    suspend fun getSourceCandidates(accountId: String, calendarId: String): List<CalendarEventEntity>

    /** The app's own mirrored events currently cached for one target account/calendar. */
    @Query(
        "SELECT * FROM calendar_events WHERE accountId = :accountId AND calendarId = :calendarId " +
            "AND mirroredFromEventId IS NOT NULL",
    )
    suspend fun getMirrorsForTarget(accountId: String, calendarId: String): List<CalendarEventEntity>
}
