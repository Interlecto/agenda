package app.ilecto.agenda.core.database

import androidx.room.TypeConverter
import app.ilecto.agenda.core.model.CalendarProvider
import java.time.Instant

class Converters {
    @TypeConverter
    fun instantToEpochMillis(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun epochMillisToInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun providerToName(value: CalendarProvider?): String? = value?.name

    @TypeConverter
    fun nameToProvider(value: String?): CalendarProvider? = value?.let(CalendarProvider::valueOf)
}
