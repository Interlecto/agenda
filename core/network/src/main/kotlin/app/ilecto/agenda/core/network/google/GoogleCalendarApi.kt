package app.ilecto.agenda.core.network.google

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Google Calendar API v3, called with an OAuth access token obtained via
 * [app.ilecto.agenda.core.network.auth.GoogleAuthManager] (scopes `calendar.readonly` +
 * `calendar.events`).
 */
interface GoogleCalendarApi {
    @GET("calendar/v3/users/me/calendarList")
    suspend fun listCalendars(
        @Header("Authorization") authorization: String,
    ): GoogleCalendarListResponse

    @GET("calendar/v3/calendars/{calendarId}/events")
    suspend fun listEvents(
        @Header("Authorization") authorization: String,
        @Path("calendarId") calendarId: String,
        @Query("timeMin") timeMin: String,
        @Query("timeMax") timeMax: String,
        @Query("singleEvents") singleEvents: Boolean = true,
        @Query("orderBy") orderBy: String = "startTime",
    ): GoogleEventsListResponse

    /** Phase 2: creates a mirrored "busy" block on a target calendar. */
    @POST("calendar/v3/calendars/{calendarId}/events")
    suspend fun insertEvent(
        @Header("Authorization") authorization: String,
        @Path("calendarId") calendarId: String,
        @Body event: GoogleEventWriteDto,
    ): GoogleEventDto

    /** Phase 2: updates a previously mirrored event whose source changed. */
    @PATCH("calendar/v3/calendars/{calendarId}/events/{eventId}")
    suspend fun patchEvent(
        @Header("Authorization") authorization: String,
        @Path("calendarId") calendarId: String,
        @Path("eventId") eventId: String,
        @Body event: GoogleEventWriteDto,
    ): GoogleEventDto

    /** Phase 2: removes a mirrored event whose source was deleted/cancelled. */
    @DELETE("calendar/v3/calendars/{calendarId}/events/{eventId}")
    suspend fun deleteEvent(
        @Header("Authorization") authorization: String,
        @Path("calendarId") calendarId: String,
        @Path("eventId") eventId: String,
    ): Response<Unit>
}
