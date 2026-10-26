package app.ilecto.agenda.feature.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ilecto.agenda.core.common.AppResult
import app.ilecto.agenda.core.domain.repository.AccountRepository
import app.ilecto.agenda.core.model.CalendarAccount
import app.ilecto.agenda.core.model.CalendarProvider
import app.ilecto.agenda.core.network.auth.GoogleAuthManager
import app.ilecto.agenda.core.network.auth.OutlookAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Linking : AuthUiState
    data class Error(val message: String) : AuthUiState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val googleAuthManager: GoogleAuthManager,
    private val outlookAuthManager: OutlookAuthManager,
) : ViewModel() {

    val linkedAccounts = accountRepository.observeAccounts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    /** [launchConsent] is the Compose-side Activity Result bridge — see AuthScreen.kt. */
    fun addGoogleAccount(context: Context, launchConsent: suspend (IntentSenderRequest) -> Intent?) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Linking
            val identity = when (val result = googleAuthManager.signIn(context)) {
                is AppResult.Success -> result.data
                is AppResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.throwable.message ?: "Google sign-in failed")
                    return@launch
                }
            }
            when (val access = googleAuthManager.requestCalendarAccess(context, launchConsent)) {
                is AppResult.Error -> {
                    _uiState.value = AuthUiState.Error(access.throwable.message ?: "Calendar access was not granted")
                    return@launch
                }
                is AppResult.Success -> Unit
            }
            val isLikelyWorkspace = !identity.email.endsWith("@gmail.com") && !identity.email.endsWith("@googlemail.com")
            val account = CalendarAccount(
                id = "google:${identity.email}",
                provider = CalendarProvider.GOOGLE,
                displayName = identity.displayName,
                emailAddress = identity.email,
                isCorporate = isLikelyWorkspace,
                colorArgb = defaultColorFor(CalendarProvider.GOOGLE, isLikelyWorkspace),
                authRef = identity.email,
            )
            save(account)
        }
    }

    fun addOutlookAccount(activity: Activity, isCorporate: Boolean) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Linking
            val identity = when (val result = outlookAuthManager.signIn(activity)) {
                is AppResult.Success -> result.data
                is AppResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.throwable.message ?: "Outlook sign-in failed")
                    return@launch
                }
            }
            val account = CalendarAccount(
                id = "outlook:${identity.homeAccountId}",
                provider = CalendarProvider.OUTLOOK,
                displayName = identity.username,
                emailAddress = identity.username,
                isCorporate = isCorporate,
                colorArgb = defaultColorFor(CalendarProvider.OUTLOOK, isCorporate),
                authRef = identity.homeAccountId,
            )
            save(account)
        }
    }

    fun unlink(account: CalendarAccount) {
        viewModelScope.launch {
            if (account.provider == CalendarProvider.OUTLOOK) {
                outlookAuthManager.removeAccount(account.authRef)
            }
            accountRepository.unlinkAccount(account.id)
        }
    }

    fun dismissError() {
        _uiState.value = AuthUiState.Idle
    }

    private suspend fun save(account: CalendarAccount) {
        when (val result = accountRepository.saveLinkedAccount(account)) {
            is AppResult.Success -> _uiState.value = AuthUiState.Idle
            is AppResult.Error -> _uiState.value = AuthUiState.Error(result.throwable.message ?: "Could not save account")
        }
    }
}

// Phase 1 keeps this simple (one shade per provider/corporate combination); letting users pick
// their own per-account color is a nice-to-have for a later phase, not an MVP requirement.
private fun defaultColorFor(provider: CalendarProvider, isCorporate: Boolean): Int = when (provider) {
    CalendarProvider.GOOGLE -> if (isCorporate) 0xFF1A73E8.toInt() else 0xFF4285F4.toInt()
    CalendarProvider.OUTLOOK -> if (isCorporate) 0xFF004E8C.toInt() else 0xFF0078D4.toInt()
}
