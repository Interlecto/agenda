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
//
// v1 -> v2 (Phase 1): added CalendarAccountEntity.authRef. Nothing has shipped yet, so this
// uses a destructive fallback (see DatabaseModule) instead of a throwaway Migration for a
// schema that's still in flux pre-release.
@Database(
    entities = [CalendarAccountEntity::class, CalendarEventEntity::class],
    version = 2,
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
