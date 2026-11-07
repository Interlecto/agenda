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
    val extendedProperties: GoogleExtendedProperties? = null,
)

/** Google represents all-day events with [date] and timed events with [dateTime]. */
@Serializable
data class GoogleEventDateTime(
    val date: String? = null,
    val dateTime: String? = null,
)

/** Only the `private` (app-only, hidden from other apps) half of Google's extendedProperties. */
@Serializable
data class GoogleExtendedProperties(
    val private: Map<String, String>? = null,
)

/**
 * Request body for `events.insert`/`events.patch` — a separate, narrower type from
 * [GoogleEventDto] because writes never carry a server-assigned [GoogleEventDto.id] or a
 * [GoogleEventDto.status].
 */
@Serializable
data class GoogleEventWriteDto(
    val summary: String? = null,
    val start: GoogleEventDateTime? = null,
    val end: GoogleEventDateTime? = null,
    val extendedProperties: GoogleExtendedProperties? = null,
)

/**
 * Keys written into [GoogleExtendedProperties.private] for events the sync engine creates —
 * see `app.ilecto.agenda.core.network.outlook.GRAPH_SOURCE_KEY_PROPERTY_ID` for the Graph
 * equivalent; both must stay in sync with how `core:data`'s mapper reads them back.
 */
const val GOOGLE_SOURCE_KEY_PROPERTY = "agendaSyncSourceId"
const val GOOGLE_RULE_ID_PROPERTY = "agendaSyncRuleId"
