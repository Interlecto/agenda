package app.ilecto.agenda.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ilecto.agenda.core.domain.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Decides AgendaNavHost's start destination. Null means "still loading". */
@HiltViewModel
class AppStartViewModel @Inject constructor(
    accountRepository: AccountRepository,
) : ViewModel() {
    val hasLinkedAccounts = accountRepository.observeAccounts()
        .map { it.isNotEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )
}
