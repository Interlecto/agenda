package app.ilecto.agenda.feature.calendar

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ilecto.agenda.core.common.AppResult
import app.ilecto.agenda.core.domain.repository.AccountRepository
import app.ilecto.agenda.core.domain.usecase.GetUnifiedEventsUseCase
import app.ilecto.agenda.core.domain.usecase.SyncAgendaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    getUnifiedEvents: GetUnifiedEventsUseCase,
    accountRepository: AccountRepository,
    private val syncAgenda: SyncAgendaUseCase,
) : ViewModel() {

    // Phase 1 TODO: drive the visible date range from the UI (day/week/agenda
    // toggle) instead of this fixed 30-day window.
    val events = getUnifiedEvents(Instant.now(), Instant.now().plus(Duration.ofDays(30)))
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val accounts = accountRepository.observeAccounts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun refresh(context: Context) {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            // Phase 2: a pull-to-refresh now also reconciles sync rules, not just re-fetches
            // events, so a rule change (new/edited/deleted) takes effect immediately instead of
            // waiting for the next 15-minute background pass.
            when (val result = syncAgenda(context)) {
                is AppResult.Success -> _errorMessage.value = null
                is AppResult.Error -> _errorMessage.value = result.throwable.message ?: "Could not refresh calendar"
            }
            _isRefreshing.value = false
        }
    }

    fun dismissError() {
        _errorMessage.value = null
    }
}
