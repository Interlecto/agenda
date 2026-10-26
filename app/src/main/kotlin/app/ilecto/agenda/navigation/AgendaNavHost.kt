package app.ilecto.agenda.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import app.ilecto.agenda.feature.auth.navigation.AUTH_ROUTE
import app.ilecto.agenda.feature.auth.navigation.authScreen
import app.ilecto.agenda.feature.calendar.navigation.CALENDAR_ROUTE
import app.ilecto.agenda.feature.calendar.navigation.calendarScreen

// Phase 2+ TODO: add the placeholder feature:sync/mail/planner/activitylog/reports graphs
// here as each phase lands, instead of a single two-screen app.
@Composable
fun AgendaNavHost(viewModel: AppStartViewModel = hiltViewModel()) {
    val hasLinkedAccounts by viewModel.hasLinkedAccounts.collectAsStateWithLifecycle()
    // Brief, deliberately blank frame while the first account-DB read resolves — the whole
    // check normally completes well within a frame, so no splash/loading UI is worth adding.
    val startDestination = hasLinkedAccounts ?: return

    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = if (startDestination) CALENDAR_ROUTE else AUTH_ROUTE,
    ) {
        authScreen(onContinueToCalendar = { navController.navigate(CALENDAR_ROUTE) })
        calendarScreen()
    }
}
