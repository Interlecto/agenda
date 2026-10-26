package app.ilecto.agenda.feature.calendar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import app.ilecto.agenda.core.designsystem.component.ComingSoonScreen

@Composable
fun CalendarRoute(modifier: Modifier = Modifier, viewModel: CalendarViewModel = hiltViewModel()) {
    ComingSoonScreen(
        title = "Unified calendar",
        phaseNote = "Merged Google + Outlook events ship in Phase 1 (see ROADMAP.md).",
        modifier = modifier,
    )
}
