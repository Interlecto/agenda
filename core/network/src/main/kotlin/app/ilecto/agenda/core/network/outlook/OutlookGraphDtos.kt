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
    val singleValueExtendedProperties: List<GraphSingleValueExtendedPropertyDto>? = null,
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

@Serializable
data class GraphSingleValueExtendedPropertyDto(
    val id: String,
    val value: String? = null,
)

/**
 * Request body for `POST`/`PATCH` on `/events` — a separate, narrower type from [GraphEventDto]
 * because writes never carry a server-assigned [GraphEventDto.id].
 */
@Serializable
data class GraphEventWriteDto(
    val subject: String? = null,
    val start: GraphDateTimeDto? = null,
    val end: GraphDateTimeDto? = null,
    val singleValueExtendedProperties: List<GraphSingleValueExtendedPropertyDto>? = null,
)

/**
 * Graph's `singleValueExtendedProperties` id is shaped `"{Type} {GUID} Name {YourName}"`, where
 * the GUID is an app-chosen namespace Microsoft never assigns — minted once and hard-coded
 * forever, since every mirrored event already on a user's calendar carries it. See
 * `app.ilecto.agenda.core.network.google.GOOGLE_SOURCE_KEY_PROPERTY` for the Google equivalent;
 * both must stay in sync with how `core:data`'s mapper reads them back.
 */
private const val AGENDA_NAMESPACE_GUID = "c42c6f18-319c-4e8b-995a-bac255657255"
const val GRAPH_SOURCE_KEY_PROPERTY_ID = "String {$AGENDA_NAMESPACE_GUID} Name AgendaSyncSourceId"
const val GRAPH_RULE_ID_PROPERTY_ID = "String {$AGENDA_NAMESPACE_GUID} Name AgendaSyncRuleId"
