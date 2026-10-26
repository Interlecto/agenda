package app.ilecto.agenda.core.network.google

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Google Calendar API v3, called with an OAuth access token obtained via
 * [app.ilecto.agenda.core.network.auth.GoogleAuthManager] (scope `calendar.readonly`).
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
}
