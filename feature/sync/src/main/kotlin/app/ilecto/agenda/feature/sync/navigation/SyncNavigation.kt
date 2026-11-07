package app.ilecto.agenda.feature.sync.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import app.ilecto.agenda.feature.sync.SyncRulesRoute

const val SYNC_ROUTE = "sync"

fun NavGraphBuilder.syncRulesScreen() {
    composable(SYNC_ROUTE) { SyncRulesRoute() }
}
