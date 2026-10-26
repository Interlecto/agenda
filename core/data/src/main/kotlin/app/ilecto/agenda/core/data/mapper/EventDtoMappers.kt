package app.ilecto.agenda.core.data.mapper

import app.ilecto.agenda.core.database.entity.CalendarEventEntity
import app.ilecto.agenda.core.network.google.GoogleEventDto
import app.ilecto.agenda.core.network.outlook.GraphEventDto
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

/** Returns null for events this app can't place on the calendar (cancelled, malformed times). */
fun GoogleEventDto.toEntity(accountId: String, calendarId: String): CalendarEventEntity? {
    if (status == "cancelled") return null
    val isAllDay = start?.date != null
    val startInstant = parseGoogleInstant(start, isAllDay) ?: return null
    val endInstant = parseGoogleInstant(end ?: start, isAllDay) ?: return null
    return CalendarEventEntity(
        id = id,
        accountId = accountId,
        calendarId = calendarId,
        title = summary ?: "(no title)",
        description = description,
        location = location,
        start = startInstant,
        end = endInstant,
        isAllDay = isAllDay,
        mirroredFromEventId = null,
    )
}

private fun parseGoogleInstant(
    dateTime: app.ilecto.agenda.core.network.google.GoogleEventDateTime?,
    isAllDay: Boolean,
): Instant? = if (isAllDay) {
    dateTime?.date?.let { LocalDate.parse(it).atStartOfDay(ZoneOffset.UTC).toInstant() }
} else {
    dateTime?.dateTime?.let { Instant.parse(it) }
}

/** Returns null for events with no usable start/end (the request pins the response to UTC). */
fun GraphEventDto.toEntity(accountId: String, calendarId: String): CalendarEventEntity? {
    val startDateTime = start?.dateTime ?: return null
    val endDateTime = end?.dateTime ?: startDateTime
    val startInstant = LocalDateTime.parse(startDateTime).toInstant(ZoneOffset.UTC)
    val endInstant = LocalDateTime.parse(endDateTime).toInstant(ZoneOffset.UTC)
    return CalendarEventEntity(
        id = id,
        accountId = accountId,
        calendarId = calendarId,
        title = subject ?: "(no title)",
        description = bodyPreview,
        location = location?.displayName,
        start = startInstant,
        end = endInstant,
        isAllDay = isAllDay,
        mirroredFromEventId = null,
    )
}
