package app.ilecto.agenda.feature.mail

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ilecto.agenda.core.common.AppResult
import app.ilecto.agenda.core.domain.repository.AccountCalendarRepository
import app.ilecto.agenda.core.domain.repository.AccountRepository
import app.ilecto.agenda.core.domain.repository.MailSuggestionRepository
import app.ilecto.agenda.core.domain.usecase.AcceptSuggestionUseCase
import app.ilecto.agenda.core.domain.usecase.ScanMailUseCase
import app.ilecto.agenda.core.model.AccountCalendar
import app.ilecto.agenda.core.model.CalendarAccount
import app.ilecto.agenda.core.model.MailSuggestion
import app.ilecto.agenda.core.model.SuggestionStatus
import app.ilecto.agenda.core.network.auth.GoogleAuthManager
import app.ilecto.agenda.core.network.auth.OutlookAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MailSuggestionsViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val mailSuggestionRepository: MailSuggestionRepository,
    private val accountCalendarRepository: AccountCalendarRepository,
    private val scanMailUseCase: ScanMailUseCase,
    private val acceptSuggestionUseCase: AcceptSuggestionUseCase,
    private val googleAuthManager: GoogleAuthManager,
    private val outlookAuthManager: OutlookAuthManager,
) : ViewModel() {

    val accounts: StateFlow<List<CalendarAccount>> = accountRepository.observeAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val suggestions: StateFlow<List<MailSuggestion>> =
        mailSuggestionRepository.observeByStatus(SuggestionStatus.PENDING)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun calendarsForAccount(accountId: String): Flow<List<AccountCalendar>> =
        accountCalendarRepository.observeCalendarsForAccount(accountId)

    fun scanNow(context: Context) {
        if (_isScanning.value) return
        viewModelScope.launch {
            _isScanning.value = true
            when (val result = scanMailUseCase(context)) {
                is AppResult.Success -> _errorMessage.value = null
                is AppResult.Error -> _errorMessage.value = result.throwable.message ?: "Could not scan mail"
            }
            _isScanning.value = false
        }
    }

    fun enableGoogleMailScan(
        account: CalendarAccount,
        context: Context,
        launchConsent: suspend (IntentSenderRequest) -> Intent?,
    ) {
        viewModelScope.launch {
            when (val result = googleAuthManager.requestMailAccess(context, launchConsent)) {
                is AppResult.Success -> setMailScanEnabled(account.id, true)
                is AppResult.Error -> _errorMessage.value = result.throwable.message ?: "Mail access was not granted"
            }
        }
    }

    fun enableOutlookMailScan(account: CalendarAccount, activity: Activity) {
        viewModelScope.launch {
            when (val result = outlookAuthManager.requestMailAccess(activity, account.authRef)) {
                is AppResult.Success -> setMailScanEnabled(account.id, true)
                is AppResult.Error -> _errorMessage.value = result.throwable.message ?: "Mail access was not granted"
            }
        }
    }

    fun disableMailScan(account: CalendarAccount) {
        viewModelScope.launch { setMailScanEnabled(account.id, false) }
    }

    fun accept(context: Context, suggestion: MailSuggestion, targetAccountId: String, targetCalendarId: String) {
        viewModelScope.launch {
            when (val result = acceptSuggestionUseCase(context, suggestion, targetAccountId, targetCalendarId)) {
                is AppResult.Success -> _errorMessage.value = null
                is AppResult.Error -> _errorMessage.value = result.throwable.message ?: "Could not add to calendar"
            }
        }
    }

    fun dismiss(suggestion: MailSuggestion) {
        viewModelScope.launch { mailSuggestionRepository.updateStatus(suggestion.id, SuggestionStatus.DISMISSED) }
    }

    private suspend fun setMailScanEnabled(accountId: String, enabled: Boolean) {
        when (val result = accountRepository.setMailScanEnabled(accountId, enabled)) {
            is AppResult.Success -> _errorMessage.value = null
            is AppResult.Error -> _errorMessage.value = result.throwable.message ?: "Could not update mail scanning"
        }
    }

    fun dismissError() {
        _errorMessage.value = null
    }
}
