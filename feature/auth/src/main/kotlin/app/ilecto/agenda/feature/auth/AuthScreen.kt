package app.ilecto.agenda.feature.auth

import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ilecto.agenda.core.model.CalendarAccount
import app.ilecto.agenda.core.model.CalendarProvider
import kotlinx.coroutines.channels.Channel

@Composable
fun AuthRoute(
    onContinueToCalendar: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val accounts by viewModel.linkedAccounts.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current

    // Bridges Google's occasional "show a consent screen" PendingIntent into the suspend call
    // GoogleAuthManager expects, without the ViewModel needing an Activity Result launcher.
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

    AuthScreen(
        accounts = accounts,
        uiState = uiState,
        onAddGoogleAccount = {
            activity?.let { viewModel.addGoogleAccount(it, launchConsent) }
        },
        onAddOutlookAccount = { isCorporate ->
            activity?.let { viewModel.addOutlookAccount(it, isCorporate) }
        },
        onUnlink = viewModel::unlink,
        onDismissError = viewModel::dismissError,
        onContinueToCalendar = onContinueToCalendar,
        modifier = modifier,
    )
}

@Composable
private fun AuthScreen(
    accounts: List<CalendarAccount>,
    uiState: AuthUiState,
    onAddGoogleAccount: () -> Unit,
    onAddOutlookAccount: (isCorporate: Boolean) -> Unit,
    onUnlink: (CalendarAccount) -> Unit,
    onDismissError: () -> Unit,
    onContinueToCalendar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(24.dp)) {
        Text(text = "Linked accounts", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Add at least one Google and one Outlook account to see them merged into one calendar.",
            style = MaterialTheme.typography.bodyMedium,
        )

        LazyColumn(modifier = Modifier.weight(1f).padding(vertical = 16.dp)) {
            items(accounts, key = { it.id }) { account ->
                AccountRow(account = account, onUnlink = { onUnlink(account) })
            }
        }

        if (uiState is AuthUiState.Error) {
            Text(
                text = uiState.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
            OutlinedButton(onClick = onDismissError) { Text("Dismiss") }
        }

        val linking = uiState is AuthUiState.Linking
        Button(onClick = onAddGoogleAccount, enabled = !linking, modifier = Modifier.fillMaxWidth()) {
            Text("Add Google account")
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            OutlinedButton(
                onClick = { onAddOutlookAccount(false) },
                enabled = !linking,
                modifier = Modifier.weight(1f),
            ) { Text("Add personal Outlook") }
            OutlinedButton(
                onClick = { onAddOutlookAccount(true) },
                enabled = !linking,
                modifier = Modifier.weight(1f),
            ) { Text("Add work/school Outlook") }
        }
        if (linking) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
            ) { CircularProgressIndicator() }
        }

        Button(
            onClick = onContinueToCalendar,
            enabled = accounts.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        ) { Text("Continue to calendar") }
    }
}

@Composable
private fun AccountRow(account: CalendarAccount, onUnlink: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val providerLabel = when (account.provider) {
                CalendarProvider.GOOGLE -> "Google"
                CalendarProvider.OUTLOOK -> "Outlook"
            }
            val kindLabel = if (account.isCorporate) "work/school" else "personal"
            Text(text = account.displayName, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "$providerLabel · $kindLabel · ${account.emailAddress}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(account.colorArgb),
            )
        }
        TextButton(onClick = onUnlink) { Text("Remove") }
    }
}
