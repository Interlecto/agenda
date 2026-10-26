package app.ilecto.agenda.core.data.di

import android.content.Context
import androidx.room.Room
import app.ilecto.agenda.core.database.AgendaDatabase
import app.ilecto.agenda.core.database.dao.CalendarAccountDao
import app.ilecto.agenda.core.database.dao.CalendarEventDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAgendaDatabase(@ApplicationContext context: Context): AgendaDatabase =
        Room.databaseBuilder(context, AgendaDatabase::class.java, AgendaDatabase.DATABASE_NAME).build()

    @Provides
    fun provideCalendarAccountDao(database: AgendaDatabase): CalendarAccountDao =
        database.calendarAccountDao()

    @Provides
    fun provideCalendarEventDao(database: AgendaDatabase): CalendarEventDao =
        database.calendarEventDao()
}
