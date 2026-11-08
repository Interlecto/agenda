package app.ilecto.agenda.core.network.outlook

import kotlinx.serialization.Serializable

@Serializable
data class GraphMessagesListResponse(
    val value: List<GraphMessageDto> = emptyList(),
)

/** `bodyPreview` is a plain-text snippet already on the list response — no second fetch needed. */
@Serializable
data class GraphMessageDto(
    val id: String,
    val subject: String? = null,
    val receivedDateTime: String? = null,
    val bodyPreview: String? = null,
)
