package app.ilecto.agenda.feature.calendar.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import app.ilecto.agenda.feature.calendar.CalendarRoute

const val CALENDAR_ROUTE = "calendar"

fun NavGraphBuilder.calendarScreen() {
    composable(CALENDAR_ROUTE) { CalendarRoute() }
}
