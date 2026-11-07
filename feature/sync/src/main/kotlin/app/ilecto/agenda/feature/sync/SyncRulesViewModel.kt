package app.ilecto.agenda.feature.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ilecto.agenda.core.common.AppResult
import app.ilecto.agenda.core.domain.repository.AccountCalendarRepository
import app.ilecto.agenda.core.domain.repository.AccountRepository
import app.ilecto.agenda.core.domain.repository.SyncRuleRepository
import app.ilecto.agenda.core.model.AccountCalendar
import app.ilecto.agenda.core.model.SyncRule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SyncRulesViewModel @Inject constructor(
    private val syncRuleRepository: SyncRuleRepository,
    accountRepository: AccountRepository,
    private val accountCalendarRepository: AccountCalendarRepository,
) : ViewModel() {

    val rules = syncRuleRepository.observeRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val accounts = accountRepository.observeAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /** Re-subscribe the caller's own `remember(accountId)` whenever the selected account changes. */
    fun calendarsForAccount(accountId: String): Flow<List<AccountCalendar>> =
        accountCalendarRepository.observeCalendarsForAccount(accountId)

    fun saveRule(
        sourceAccountId: String,
        sourceCalendarId: String,
        targetAccountId: String,
        targetCalendarId: String,
        redactionTemplate: String,
        keywordTrigger: String?,
    ) {
        val rule = SyncRule(
            id = UUID.randomUUID().toString(),
            sourceAccountId = sourceAccountId,
            sourceCalendarId = sourceCalendarId,
            targetAccountId = targetAccountId,
            targetCalendarId = targetCalendarId,
            redactionTemplate = redactionTemplate,
            keywordTrigger = keywordTrigger,
            enabled = true,
        )
        viewModelScope.launch {
            when (val result = syncRuleRepository.saveRule(rule)) {
                is AppResult.Success -> _errorMessage.value = null
                is AppResult.Error -> _errorMessage.value = result.throwable.message ?: "Could not save rule"
            }
        }
    }

    fun setEnabled(rule: SyncRule, enabled: Boolean) {
        viewModelScope.launch { syncRuleRepository.saveRule(rule.copy(enabled = enabled)) }
    }

    fun deleteRule(ruleId: String) {
        viewModelScope.launch { syncRuleRepository.deleteRule(ruleId) }
    }

    fun dismissError() {
        _errorMessage.value = null
    }
}
