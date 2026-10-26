package app.ilecto.agenda.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ilecto.agenda.core.domain.usecase.GetUnifiedEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    getUnifiedEvents: GetUnifiedEventsUseCase,
) : ViewModel() {
    // Phase 1 TODO: drive the visible date range from the UI (day/week/agenda
    // toggle) instead of this fixed one-week window.
    val events = getUnifiedEvents(Instant.now(), Instant.now().plus(Duration.ofDays(7)))
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )
}
