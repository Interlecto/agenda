package app.ilecto.agenda.feature.mail

import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ilecto.agenda.core.designsystem.component.ComingSoonScreen
import app.ilecto.agenda.core.model.AccountCalendar
import app.ilecto.agenda.core.model.CalendarAccount
import app.ilecto.agenda.core.model.CalendarProvider
import app.ilecto.agenda.core.model.MailSuggestion
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun MailSuggestionsRoute(modifier: Modifier = Modifier, viewModel: MailSuggestionsViewModel = hiltViewModel()) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val context = LocalContext.current

    // Same IntentSender bridge AuthScreen.kt uses for Google's occasional consent screen.
    val consentChannel = remember { Channel<Intent?>(Channel.CONFLATED) }
    val consentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result -> consentChannel.trySend(result.data) }
    val launchConsent: suspend (IntentSenderRequest) -> Intent? = remember {
        { request ->
            consentLauncher.launch(request)
            consentChannel.receive()
        }
    }

    if (accounts.isEmpty()) {
        ComingSoonScreen(
            title = "Link an account first",
            phaseNote = "Go back and add a Google or Outlook account before scanning mail for events.",
            modifier = modifier,
        )
        return
    }

    MailSuggestionsScreen(
        accounts = accounts,
        suggestions = suggestions,
        isScanning = isScanning,
        errorMessage = errorMessage,
        calendarsForAccount = viewModel::calendarsForAccount,
        onScanNow = { viewModel.scanNow(context) },
        onToggleMailScan = { account, enabled ->
            if (enabled) {
                when (account.provider) {
                    CalendarProvider.GOOGLE -> viewModel.enableGoogleMailScan(account, context, launchConsent)
                    CalendarProvider.OUTLOOK -> activity?.let { viewModel.enableOutlookMailScan(account, it) }
                }
            } else {
                viewModel.disableMailScan(account)
            }
        },
        onAccept = { suggestion, targetAccountId, targetCalendarId ->
            viewModel.accept(context, suggestion, targetAccountId, targetCalendarId)
        },
        onDismiss = viewModel::dismiss,
        onDismissError = viewModel::dismissError,
        modifier = modifier,
    )
}

@Composable
private fun MailSuggestionsScreen(
    accounts: List<CalendarAccount>,
    suggestions: List<MailSuggestion>,
    isScanning: Boolean,
    errorMessage: String?,
    calendarsForAccount: (String) -> Flow<List<AccountCalendar>>,
    onScanNow: () -> Unit,
    onToggleMailScan: (CalendarAccount, Boolean) -> Unit,
    onAccept: (MailSuggestion, String, String) -> Unit,
    onDismiss: (MailSuggestion) -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Mail suggestions", style = MaterialTheme.typography.headlineSmall)
            OutlinedButton(onClick = onScanNow, enabled = !isScanning) {
                Text(if (isScanning) "Scanning…" else "Scan now")
            }
        }
        Text(
            text = "Local, on-device heuristics only — nothing is ever added without your approval.",
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

        Text(text = "Scan mail for", style = MaterialTheme.typography.titleSmall)
        accounts.forEach { account ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = account.displayName, style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = account.mailScanEnabled,
                    onCheckedChange = { onToggleMailScan(account, it) },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (suggestions.isEmpty()) {
            Text(text = "No suggestions right now.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn {
                items(suggestions, key = { it.id }) { suggestion ->
                    SuggestionRow(
                        suggestion = suggestion,
                        accounts = accounts,
                        calendarsForAccount = calendarsForAccount,
                        onAccept = onAccept,
                        onDismiss = { onDismiss(suggestion) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionRow(
    suggestion: MailSuggestion,
    accounts: List<CalendarAccount>,
    calendarsForAccount: (String) -> Flow<List<AccountCalendar>>,
    onAccept: (MailSuggestion, String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    val zoneId = remember { ZoneId.systemDefault() }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = suggestion.title, style = MaterialTheme.typography.bodyLarge)
        val dateLabel = if (suggestion.isAllDay) {
            suggestion.start.atZone(zoneId).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        } else {
            suggestion.start.atZone(zoneId).format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT))
        }
        Text(text = dateLabel, style = MaterialTheme.typography.bodySmall)
        Text(text = suggestion.sourceSnippet, style = MaterialTheme.typography.bodySmall, maxLines = 2)

        if (showPicker) {
            AcceptPicker(
                accounts = accounts,
                calendarsForAccount = calendarsForAccount,
                onConfirm = { accountId, calendarId ->
                    onAccept(suggestion, accountId, calendarId)
                    showPicker = false
                },
                onCancel = { showPicker = false },
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { showPicker = true }) { Text("Add to calendar") }
                TextButton(onClick = onDismiss) { Text("Dismiss") }
            }
        }
    }
}

@Composable
private fun AcceptPicker(
    accounts: List<CalendarAccount>,
    calendarsForAccount: (String) -> Flow<List<AccountCalendar>>,
    onConfirm: (String, String) -> Unit,
    onCancel: () -> Unit,
) {
    var accountId by remember { mutableStateOf<String?>(null) }
    var calendarId by remember { mutableStateOf<String?>(null) }
    val selectedAccount = accounts.find { it.id == accountId }
    val calendars by remember(accountId) {
        accountId?.let(calendarsForAccount) ?: flowOf(emptyList())
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Dropdown(
            label = selectedAccount?.displayName ?: "Account",
            options = accounts.map { it.id to it.displayName },
            onSelect = { accountId = it; calendarId = null },
        )
        if (accountId != null) {
            Dropdown(
                label = calendars.find { it.calendarId == calendarId }?.displayName ?: "Calendar",
                options = calendars.map { it.calendarId to it.displayName },
                onSelect = { calendarId = it },
            )
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            enabled = accountId != null && calendarId != null,
            onClick = { onConfirm(accountId!!, calendarId!!) },
        ) { Text("Confirm") }
        TextButton(onClick = onCancel) { Text("Cancel") }
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
