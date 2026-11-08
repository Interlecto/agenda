package app.ilecto.agenda.core.network.google

import kotlinx.serialization.Serializable

@Serializable
data class GmailMessagesListResponse(
    val messages: List<GmailMessageRef> = emptyList(),
)

@Serializable
data class GmailMessageRef(
    val id: String,
)

/** A `format=metadata` response: headers only, no body — cheap enough to fetch per-candidate. */
@Serializable
data class GmailMessageDto(
    val id: String,
    val snippet: String? = null,
    val payload: GmailPayloadDto? = null,
)

@Serializable
data class GmailPayloadDto(
    val headers: List<GmailHeaderDto> = emptyList(),
)

@Serializable
data class GmailHeaderDto(
    val name: String,
    val value: String? = null,
)
