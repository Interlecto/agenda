package app.ilecto.agenda.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import app.ilecto.agenda.feature.auth.navigation.AUTH_ROUTE
import app.ilecto.agenda.feature.auth.navigation.authScreen
import app.ilecto.agenda.feature.calendar.navigation.CALENDAR_ROUTE
import app.ilecto.agenda.feature.calendar.navigation.calendarScreen

// Phase 1 TODO: start at AUTH_ROUTE only when no account is linked yet, and add the
// placeholder feature:sync/mail/planner/activitylog/reports graphs here as each phase
// lands, instead of a single two-screen app.
@Composable
fun AgendaNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AUTH_ROUTE) {
        authScreen(onContinueToCalendar = { navController.navigate(CALENDAR_ROUTE) })
        calendarScreen()
    }
}
