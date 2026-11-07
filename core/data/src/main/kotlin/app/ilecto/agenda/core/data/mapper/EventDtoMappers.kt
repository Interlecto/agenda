package app.ilecto.agenda.core.data.mapper

import app.ilecto.agenda.core.database.entity.CalendarEventEntity
import app.ilecto.agenda.core.network.google.GOOGLE_RULE_ID_PROPERTY
import app.ilecto.agenda.core.network.google.GOOGLE_SOURCE_KEY_PROPERTY
import app.ilecto.agenda.core.network.google.GoogleEventDateTime
import app.ilecto.agenda.core.network.google.GoogleEventDto
import app.ilecto.agenda.core.network.google.GoogleEventWriteDto
import app.ilecto.agenda.core.network.google.GoogleExtendedProperties
import app.ilecto.agenda.core.network.outlook.GRAPH_RULE_ID_PROPERTY_ID
import app.ilecto.agenda.core.network.outlook.GRAPH_SOURCE_KEY_PROPERTY_ID
import app.ilecto.agenda.core.network.outlook.GraphDateTimeDto
import app.ilecto.agenda.core.network.outlook.GraphEventDto
import app.ilecto.agenda.core.network.outlook.GraphEventWriteDto
import app.ilecto.agenda.core.network.outlook.GraphSingleValueExtendedPropertyDto
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
        mirroredFromEventId = extendedProperties?.private?.get(GOOGLE_SOURCE_KEY_PROPERTY),
        mirroredByRuleId = extendedProperties?.private?.get(GOOGLE_RULE_ID_PROPERTY),
    )
}

/**
 * Builds the request body for `GoogleCalendarApi.insertEvent`/`patchEvent`: a redacted "busy"
 * block carrying both sync markers. Always mirrors as a timed (non-all-day) event using the
 * source's UTC instants — Phase 2 doesn't special-case all-day source events, which will show up
 * as a block spanning midnight-to-midnight UTC converted to the target calendar's local time; a
 * known simplification, not a bug.
 */
fun buildGoogleMirrorWrite(
    title: String,
    start: Instant,
    end: Instant,
    sourceKey: String,
    ruleId: String,
) = GoogleEventWriteDto(
    summary = title,
    start = GoogleEventDateTime(dateTime = start.toString()),
    end = GoogleEventDateTime(dateTime = end.toString()),
    extendedProperties = GoogleExtendedProperties(
        private = mapOf(GOOGLE_SOURCE_KEY_PROPERTY to sourceKey, GOOGLE_RULE_ID_PROPERTY to ruleId),
    ),
)

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
        mirroredFromEventId = singleValueExtendedProperties
            ?.firstOrNull { it.id == GRAPH_SOURCE_KEY_PROPERTY_ID }
            ?.value,
        mirroredByRuleId = singleValueExtendedProperties
            ?.firstOrNull { it.id == GRAPH_RULE_ID_PROPERTY_ID }
            ?.value,
    )
}

/** Graph equivalent of [buildGoogleMirrorWrite] — see its doc comment for the all-day caveat. */
fun buildGraphMirrorWrite(
    title: String,
    start: Instant,
    end: Instant,
    sourceKey: String,
    ruleId: String,
) = GraphEventWriteDto(
    subject = title,
    // Graph's dateTime field is a bare local date-time (no "Z"/offset) paired with timeZone —
    // unlike Instant.toString(), which would append "Z" and be misread as a literal UTC marker
    // concatenated onto a local time rather than parsed as one.
    start = GraphDateTimeDto(dateTime = LocalDateTime.ofInstant(start, ZoneOffset.UTC).toString(), timeZone = "UTC"),
    end = GraphDateTimeDto(dateTime = LocalDateTime.ofInstant(end, ZoneOffset.UTC).toString(), timeZone = "UTC"),
    singleValueExtendedProperties = listOf(
        GraphSingleValueExtendedPropertyDto(id = GRAPH_SOURCE_KEY_PROPERTY_ID, value = sourceKey),
        GraphSingleValueExtendedPropertyDto(id = GRAPH_RULE_ID_PROPERTY_ID, value = ruleId),
    ),
)
