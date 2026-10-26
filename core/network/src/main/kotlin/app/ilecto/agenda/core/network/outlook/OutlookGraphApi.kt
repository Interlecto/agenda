package app.ilecto.agenda.core.network.outlook

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Microsoft Graph, called with an OAuth access token obtained via
 * [app.ilecto.agenda.core.network.auth.OutlookAuthManager] (scope `Calendars.Read`). Works
 * against both personal Microsoft and work/school (Entra ID) tenants.
 */
interface OutlookGraphApi {
    @GET("v1.0/me/calendars")
    suspend fun listCalendars(
        @Header("Authorization") authorization: String,
    ): GraphCalendarListResponse

    // Pin the response to UTC so every event's dateTime/timeZone pair is directly parseable
    // without needing an on-device IANA timezone lookup for whatever zone the calendar is in.
    @Headers("Prefer: outlook.timezone=\"UTC\"")
    @GET("v1.0/me/calendars/{calendarId}/calendarView")
    suspend fun listEvents(
        @Header("Authorization") authorization: String,
        @Path("calendarId") calendarId: String,
        @Query("startDateTime") startDateTime: String,
        @Query("endDateTime") endDateTime: String,
    ): GraphEventsListResponse
}
