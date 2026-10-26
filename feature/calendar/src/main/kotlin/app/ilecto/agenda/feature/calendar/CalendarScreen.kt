package app.ilecto.agenda.feature.calendar

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ilecto.agenda.core.designsystem.component.ComingSoonScreen
import app.ilecto.agenda.core.model.CalendarAccount
import app.ilecto.agenda.core.model.CalendarEvent
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun CalendarRoute(modifier: Modifier = Modifier, viewModel: CalendarViewModel = hiltViewModel()) {
    val events by viewModel.events.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val activity = LocalActivity.current

    if (accounts.isEmpty()) {
        ComingSoonScreen(
            title = "No linked accounts yet",
            phaseNote = "Go back and add a Google or Outlook account to see your unified calendar.",
            modifier = modifier,
        )
        return
    }

    CalendarScreen(
        events = events,
        accounts = accounts,
        isRefreshing = isRefreshing,
        errorMessage = errorMessage,
        onRefresh = { activity?.let(viewModel::refresh) },
        onDismissError = viewModel::dismissError,
        modifier = modifier,
    )
}

@Composable
private fun CalendarScreen(
    events: List<CalendarEvent>,
    accounts: List<CalendarAccount>,
    isRefreshing: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accountsById = remember(accounts) { accounts.associateBy { it.id } }
    val zoneId = remember { ZoneId.systemDefault() }
    val groupedByDay = remember(events) {
        events.sortedBy { it.start }.groupBy { it.start.atZone(zoneId).toLocalDate() }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Agenda", style = MaterialTheme.typography.headlineSmall)
            OutlinedButton(onClick = onRefresh, enabled = !isRefreshing) {
                Text(if (isRefreshing) "Refreshing…" else "Refresh")
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
            Button(onClick = onDismissError) { Text("Dismiss") }
        }

        if (isRefreshing && events.isEmpty()) {
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        if (events.isEmpty()) {
            Text(
                text = "No events in the next 30 days.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 24.dp),
            )
            return@Column
        }

        LazyColumn {
            groupedByDay.forEach { (day, dayEvents) ->
                item(key = day) {
                    Text(
                        text = day.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                    )
                }
                items(dayEvents, key = { it.id }) { event ->
                    EventRow(event = event, account = accountsById[event.accountId], zoneId = zoneId)
                }
            }
        }
    }
}

@Composable
private fun EventRow(event: CalendarEvent, account: CalendarAccount?, zoneId: ZoneId) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(
            modifier = Modifier
                .size(10.dp)
                .background(color = Color(account?.colorArgb ?: 0xFF9E9E9E.toInt()), shape = CircleShape),
        ) {}
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(text = event.title, style = MaterialTheme.typography.bodyLarge)
            val timeLabel = if (event.isAllDay) {
                "All day"
            } else {
                val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                "${event.start.atZone(zoneId).format(formatter)} – ${event.end.atZone(zoneId).format(formatter)}"
            }
            val locationSuffix = event.location?.let { " · $it" } ?: ""
            Text(
                text = "$timeLabel$locationSuffix",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
