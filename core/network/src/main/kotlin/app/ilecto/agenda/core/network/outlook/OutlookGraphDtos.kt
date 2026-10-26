package app.ilecto.agenda.core.network.outlook

import kotlinx.serialization.Serializable

@Serializable
data class GraphCalendarListResponse(
    val value: List<GraphCalendarDto> = emptyList(),
)

@Serializable
data class GraphCalendarDto(
    val id: String,
    val name: String? = null,
)

@Serializable
data class GraphEventsListResponse(
    val value: List<GraphEventDto> = emptyList(),
)

@Serializable
data class GraphEventDto(
    val id: String,
    val subject: String? = null,
    val bodyPreview: String? = null,
    val isAllDay: Boolean = false,
    val location: GraphLocationDto? = null,
    val start: GraphDateTimeDto? = null,
    val end: GraphDateTimeDto? = null,
)

@Serializable
data class GraphLocationDto(
    val displayName: String? = null,
)

/** Microsoft Graph always sends a local date/time plus an IANA-ish [timeZone] name. */
@Serializable
data class GraphDateTimeDto(
    val dateTime: String? = null,
    val timeZone: String? = null,
)
