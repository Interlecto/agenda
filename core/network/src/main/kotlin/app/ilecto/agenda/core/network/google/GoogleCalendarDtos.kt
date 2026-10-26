package app.ilecto.agenda.core.network.google

import kotlinx.serialization.Serializable

@Serializable
data class GoogleCalendarListResponse(
    val items: List<GoogleCalendarListEntry> = emptyList(),
)

@Serializable
data class GoogleCalendarListEntry(
    val id: String,
    val summary: String? = null,
)

@Serializable
data class GoogleEventsListResponse(
    val items: List<GoogleEventDto> = emptyList(),
)

@Serializable
data class GoogleEventDto(
    val id: String,
    val summary: String? = null,
    val description: String? = null,
    val location: String? = null,
    val status: String? = null,
    val start: GoogleEventDateTime? = null,
    val end: GoogleEventDateTime? = null,
)

/** Google represents all-day events with [date] and timed events with [dateTime]. */
@Serializable
data class GoogleEventDateTime(
    val date: String? = null,
    val dateTime: String? = null,
)
