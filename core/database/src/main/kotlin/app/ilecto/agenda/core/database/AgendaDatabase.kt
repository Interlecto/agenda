package app.ilecto.agenda.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.ilecto.agenda.core.database.dao.CalendarAccountDao
import app.ilecto.agenda.core.database.dao.CalendarEventDao
import app.ilecto.agenda.core.database.entity.CalendarAccountEntity
import app.ilecto.agenda.core.database.entity.CalendarEventEntity

// Phase 4/5/6 will add Task, Project and ActivityLogEntry entities + DAOs here,
// each with its own migration, once those features are actually built.
@Database(
    entities = [CalendarAccountEntity::class, CalendarEventEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AgendaDatabase : RoomDatabase() {
    abstract fun calendarAccountDao(): CalendarAccountDao
    abstract fun calendarEventDao(): CalendarEventDao

    companion object {
        const val DATABASE_NAME = "agenda.db"
    }
}
