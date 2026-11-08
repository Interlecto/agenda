package app.ilecto.agenda.core.network.outlook

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Microsoft Graph, called with an OAuth access token obtained via
 * [app.ilecto.agenda.core.network.auth.OutlookAuthManager] (scope `Calendars.ReadWrite`). Works
 * against both personal Microsoft and work/school (Entra ID) tenants.
 */
interface OutlookGraphApi {
    @GET("v1.0/me/calendars")
    suspend fun listCalendars(
        @Header("Authorization") authorization: String,
    ): GraphCalendarListResponse

    // Pin the response to UTC so every event's dateTime/timeZone pair is directly parseable
    // without needing an on-device IANA timezone lookup for whatever zone the calendar is in.
    // Graph doesn't return extended properties by default (unlike Google) — $expand with a
    // $filter scoped to just this app's two marker ids pulls back only those, not every other
    // app's hidden properties on the event.
    @Headers("Prefer: outlook.timezone=\"UTC\"")
    @GET("v1.0/me/calendars/{calendarId}/calendarView")
    suspend fun listEvents(
        @Header("Authorization") authorization: String,
        @Path("calendarId") calendarId: String,
        @Query("startDateTime") startDateTime: String,
        @Query("endDateTime") endDateTime: String,
        @Query("\$expand") expand: String = EXTENDED_PROPERTIES_EXPAND,
    ): GraphEventsListResponse

    /** Phase 2: creates a mirrored "busy" block on a target calendar. */
    @Headers("Prefer: outlook.timezone=\"UTC\"")
    @POST("v1.0/me/calendars/{calendarId}/events")
    suspend fun createEvent(
        @Header("Authorization") authorization: String,
        @Path("calendarId") calendarId: String,
        @Body event: GraphEventWriteDto,
    ): GraphEventDto

    /** Phase 2: updates a previously mirrored event whose source changed. */
    @Headers("Prefer: outlook.timezone=\"UTC\"")
    @PATCH("v1.0/me/calendars/{calendarId}/events/{eventId}")
    suspend fun updateEvent(
        @Header("Authorization") authorization: String,
        @Path("calendarId") calendarId: String,
        @Path("eventId") eventId: String,
        @Body event: GraphEventWriteDto,
    ): GraphEventDto

    /** Phase 2: removes a mirrored event whose source was deleted/cancelled. */
    @DELETE("v1.0/me/calendars/{calendarId}/events/{eventId}")
    suspend fun deleteEvent(
        @Header("Authorization") authorization: String,
        @Path("calendarId") calendarId: String,
        @Path("eventId") eventId: String,
    ): Response<Unit>

    /**
     * Phase 3: recent messages for the mail scanner, called with a `Mail.Read`-scoped token.
     * [filter] and [orderby] both reference `receivedDateTime` — required together, or Graph
     * rejects the combination as an "inefficient filter".
     */
    @GET("v1.0/me/messages")
    suspend fun listMessages(
        @Header("Authorization") authorization: String,
        @Query("\$filter") filter: String,
        @Query("\$select") select: String = "id,subject,receivedDateTime,bodyPreview",
        @Query("\$orderby") orderby: String = "receivedDateTime desc",
        @Query("\$top") top: Int = 25,
    ): GraphMessagesListResponse
}

private const val EXTENDED_PROPERTIES_EXPAND =
    "singleValueExtendedProperties(\$filter=id eq '$GRAPH_SOURCE_KEY_PROPERTY_ID' " +
        "or id eq '$GRAPH_RULE_ID_PROPERTY_ID')"
