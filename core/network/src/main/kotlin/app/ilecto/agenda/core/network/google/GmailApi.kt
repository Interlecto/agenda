package app.ilecto.agenda.core.network.google

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Gmail API v1, called with an OAuth access token obtained via
 * [app.ilecto.agenda.core.network.auth.GoogleAuthManager.requestMailAccess] (scope
 * `gmail.readonly`). Lives on a different host (`gmail.googleapis.com`) than the Calendar API,
 * so it's registered against its own `@GmailRetrofit` instance, not `@GoogleRetrofit`.
 */
interface GmailApi {
    /** Returns only ids — [getMessage] is needed to read headers/snippet for each one. */
    @GET("gmail/v1/users/me/messages")
    suspend fun listMessages(
        @Header("Authorization") authorization: String,
        @Query("q") q: String,
        @Query("maxResults") maxResults: Int = 25,
    ): GmailMessagesListResponse

    /** `format=metadata` + [metadataHeaders] skips the message body — cheap for heuristics. */
    @GET("gmail/v1/users/me/messages/{id}")
    suspend fun getMessage(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
        @Query("format") format: String = "metadata",
        @Query("metadataHeaders") metadataHeaders: List<String> = listOf("Subject", "Date"),
    ): GmailMessageDto
}
