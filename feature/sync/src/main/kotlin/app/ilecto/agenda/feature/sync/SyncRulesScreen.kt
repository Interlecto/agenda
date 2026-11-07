package app.ilecto.agenda.feature.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ilecto.agenda.core.designsystem.component.ComingSoonScreen
import app.ilecto.agenda.core.model.AccountCalendar
import app.ilecto.agenda.core.model.CalendarAccount
import app.ilecto.agenda.core.model.SyncRule

@Composable
fun SyncRulesRoute(modifier: Modifier = Modifier, viewModel: SyncRulesViewModel = hiltViewModel()) {
    val rules by viewModel.rules.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    if (accounts.size < 2) {
        ComingSoonScreen(
            title = "Link two accounts first",
            phaseNote = "A sync rule mirrors events from one linked calendar onto another — " +
                "go back and add at least two accounts (e.g. a personal Google and a work Outlook).",
            modifier = modifier,
        )
        return
    }

    SyncRulesScreen(
        rules = rules,
        accounts = accounts,
        errorMessage = errorMessage,
        calendarsForAccount = viewModel::calendarsForAccount,
        onSaveRule = viewModel::saveRule,
        onSetEnabled = viewModel::setEnabled,
        onDeleteRule = viewModel::deleteRule,
        onDismissError = viewModel::dismissError,
        modifier = modifier,
    )
}

@Composable
private fun SyncRulesScreen(
    rules: List<SyncRule>,
    accounts: List<CalendarAccount>,
    errorMessage: String?,
    calendarsForAccount: (String) -> kotlinx.coroutines.flow.Flow<List<AccountCalendar>>,
    onSaveRule: (String, String, String, String, String, String?) -> Unit,
    onSetEnabled: (SyncRule, Boolean) -> Unit,
    onDeleteRule: (String) -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showForm by remember { mutableStateOf(false) }
    val accountsById = remember(accounts) { accounts.associateBy { it.id } }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Cross-calendar sync", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Mirror events from one calendar onto another as a redacted \"busy\" block.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
            TextButton(onClick = onDismissError) { Text("Dismiss") }
        }

        if (rules.isEmpty() && !showForm) {
            Text(
                text = "No sync rules yet.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        } else {
            LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                items(rules, key = { it.id }) { rule ->
                    RuleRow(
                        rule = rule,
                        sourceAccountName = accountsById[rule.sourceAccountId]?.displayName ?: "(removed account)",
                        targetAccountName = accountsById[rule.targetAccountId]?.displayName ?: "(removed account)",
                        onSetEnabled = { onSetEnabled(rule, it) },
                        onDelete = { onDeleteRule(rule.id) },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showForm) {
            NewRuleForm(
                accounts = accounts,
                calendarsForAccount = calendarsForAccount,
                onSave = { source, sourceCal, target, targetCal, template, keyword ->
                    onSaveRule(source, sourceCal, target, targetCal, template, keyword)
                    showForm = false
                },
                onCancel = { showForm = false },
            )
        } else {
            OutlinedButton(onClick = { showForm = true }) { Text("+ New rule") }
        }
    }
}

@Composable
private fun RuleRow(
    rule: SyncRule,
    sourceAccountName: String,
    targetAccountName: String,
    onSetEnabled: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$sourceAccountName (${rule.sourceCalendarId}) → $targetAccountName (${rule.targetCalendarId})",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "\"${rule.redactionTemplate}\"" +
                        (rule.keywordTrigger?.let { " · only events matching \"$it\"" } ?: " · whole calendar"),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Switch(checked = rule.enabled, onCheckedChange = onSetEnabled)
            TextButton(onClick = onDelete) { Text("Delete") }
        }
    }
}

@Composable
private fun NewRuleForm(
    accounts: List<CalendarAccount>,
    calendarsForAccount: (String) -> kotlinx.coroutines.flow.Flow<List<AccountCalendar>>,
    onSave: (String, String, String, String, String, String?) -> Unit,
    onCancel: () -> Unit,
) {
    var sourceAccountId by remember { mutableStateOf<String?>(null) }
    var sourceCalendarId by remember { mutableStateOf<String?>(null) }
    var targetAccountId by remember { mutableStateOf<String?>(null) }
    var targetCalendarId by remember { mutableStateOf<String?>(null) }
    var template by remember { mutableStateOf("Busy") }
    var keyword by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Source", style = MaterialTheme.typography.titleSmall)
        AccountCalendarPicker(
            accounts = accounts,
            selectedAccountId = sourceAccountId,
            selectedCalendarId = sourceCalendarId,
            calendarsForAccount = calendarsForAccount,
            onAccountSelected = { sourceAccountId = it; sourceCalendarId = null },
            onCalendarSelected = { sourceCalendarId = it },
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Target", style = MaterialTheme.typography.titleSmall)
        AccountCalendarPicker(
            accounts = accounts,
            selectedAccountId = targetAccountId,
            selectedCalendarId = targetCalendarId,
            calendarsForAccount = calendarsForAccount,
            onAccountSelected = { targetAccountId = it; targetCalendarId = null },
            onCalendarSelected = { targetCalendarId = it },
        )

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = template,
            onValueChange = { template = it },
            label = { Text("Block title (e.g. \"Busy — Medical\")") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = keyword,
            onValueChange = { keyword = it },
            label = { Text("Only events containing (optional)") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val canSave = sourceAccountId != null && sourceCalendarId != null &&
                targetAccountId != null && targetCalendarId != null &&
                template.isNotBlank() && sourceAccountId != targetAccountId
            Button(
                enabled = canSave,
                onClick = {
                    onSave(
                        sourceAccountId!!,
                        sourceCalendarId!!,
                        targetAccountId!!,
                        targetCalendarId!!,
                        template.trim(),
                        keyword.trim().ifBlank { null },
                    )
                },
            ) { Text("Save rule") }
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}

@Composable
private fun AccountCalendarPicker(
    accounts: List<CalendarAccount>,
    selectedAccountId: String?,
    selectedCalendarId: String?,
    calendarsForAccount: (String) -> kotlinx.coroutines.flow.Flow<List<AccountCalendar>>,
    onAccountSelected: (String) -> Unit,
    onCalendarSelected: (String) -> Unit,
) {
    val selectedAccount = accounts.find { it.id == selectedAccountId }
    val calendars by remember(selectedAccountId) {
        selectedAccountId?.let { calendarsForAccount(it) } ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Dropdown(
            label = selectedAccount?.displayName ?: "Choose account",
            options = accounts.map { it.id to it.displayName },
            onSelect = onAccountSelected,
        )
        if (selectedAccountId != null) {
            Dropdown(
                label = calendars.find { it.calendarId == selectedCalendarId }?.displayName ?: "Choose calendar",
                options = calendars.map { it.calendarId to it.displayName },
                onSelect = onCalendarSelected,
            )
        }
    }
}

@Composable
private fun Dropdown(label: String, options: List<Pair<String, String>>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        OutlinedButton(onClick = { expanded = true }, enabled = options.isNotEmpty()) { Text(label) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (id, name) ->
                DropdownMenuItem(text = { Text(name) }, onClick = { onSelect(id); expanded = false })
            }
        }
    }
}
