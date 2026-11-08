package app.ilecto.agenda.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.ilecto.agenda.core.database.dao.AccountCalendarDao
import app.ilecto.agenda.core.database.dao.CalendarAccountDao
import app.ilecto.agenda.core.database.dao.CalendarEventDao
import app.ilecto.agenda.core.database.dao.MailSuggestionDao
import app.ilecto.agenda.core.database.dao.SyncRuleDao
import app.ilecto.agenda.core.database.entity.AccountCalendarEntity
import app.ilecto.agenda.core.database.entity.CalendarAccountEntity
import app.ilecto.agenda.core.database.entity.CalendarEventEntity
import app.ilecto.agenda.core.database.entity.MailSuggestionEntity
import app.ilecto.agenda.core.database.entity.SyncRuleEntity

// Phase 4/5/6 will add Task, Project and ActivityLogEntry entities + DAOs here,
// each with its own migration, once those features are actually built.
//
// v1 -> v2 (Phase 1): added CalendarAccountEntity.authRef.
// v2 -> v3 (Phase 2): added SyncRuleEntity, AccountCalendarEntity, and
// CalendarEventEntity.mirroredByRuleId.
// v3 -> v4 (Phase 3): added CalendarAccountEntity.mailScanEnabled and MailSuggestionEntity.
// Nothing has shipped yet, so this keeps using a destructive fallback (see DatabaseModule)
// instead of a throwaway Migration for a schema that's still in flux pre-release.
@Database(
    entities = [
        CalendarAccountEntity::class,
        CalendarEventEntity::class,
        SyncRuleEntity::class,
        AccountCalendarEntity::class,
        MailSuggestionEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AgendaDatabase : RoomDatabase() {
    abstract fun calendarAccountDao(): CalendarAccountDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun syncRuleDao(): SyncRuleDao
    abstract fun accountCalendarDao(): AccountCalendarDao
    abstract fun mailSuggestionDao(): MailSuggestionDao

    companion object {
        const val DATABASE_NAME = "agenda.db"
    }
}
