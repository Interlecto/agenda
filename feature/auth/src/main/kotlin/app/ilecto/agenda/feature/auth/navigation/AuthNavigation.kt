package app.ilecto.agenda.feature.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import app.ilecto.agenda.feature.auth.AuthRoute

const val AUTH_ROUTE = "auth"

fun NavGraphBuilder.authScreen(onContinueToCalendar: () -> Unit) {
    composable(AUTH_ROUTE) { AuthRoute(onContinueToCalendar = onContinueToCalendar) }
}
