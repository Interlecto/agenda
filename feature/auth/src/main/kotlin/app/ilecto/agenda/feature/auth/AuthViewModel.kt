package app.ilecto.agenda.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ilecto.agenda.core.domain.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    accountRepository: AccountRepository,
) : ViewModel() {
    // Phase 1 TODO: expose sign-in actions (link/unlink per provider) and surface
    // AppResult errors to the UI. For now this just proves the Hilt + repository
    // wiring compiles end-to-end.
    val linkedAccounts = accountRepository.observeAccounts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )
}
