package com.kristianolsson.didmyteamwin.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.work.WorkInfo
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    viewModel: DebugViewModel,
    onBack: () -> Unit,
) {
    val items by viewModel.items.collectAsState()
    val lastRefresh by viewModel.lastRefresh.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debug") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (lastRefresh > 0) {
                item {
                    Text(
                        text = "Updated ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(lastRefresh))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                }
            }

            items(items, key = { it.team.idTeam }) { info ->
                DebugCard(info)
            }
        }
    }
}

@Composable
private fun DebugCard(info: TeamDebugInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = info.team.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "ID: ${info.team.idTeam}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(thickness = 0.5.dp)
            Spacer(Modifier.height(10.dp))

            DebugRow("Next event", info.team.nextEventName ?: "—")
            DebugRow("Event ID", info.team.nextEventId ?: "—")
            DebugRow("Kickoff (UTC)", info.team.nextEventTimestamp ?: "—")
            DebugRow("Kickoff (local)", localTime(info.team.nextEventTimestamp))
            DebugRow("Retry count", info.team.retryCount.toString())
            DebugRow("Last result", info.team.lastResultSummary ?: "—")

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(thickness = 0.5.dp)
            Spacer(Modifier.height(10.dp))

            DebugRow(
                "game_check",
                buildWorkLabel(info.gameCheckStatus, info.gameCheckRunAtMillis),
                stateColor(info.gameCheckStatus),
            )
            DebugRow(
                "event_poll",
                buildWorkLabel(info.eventPollStatus, info.eventPollRunAtMillis),
                stateColor(info.eventPollStatus),
            )
        }
    }
}

@Composable
private fun DebugRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = valueColor,
            modifier = Modifier.weight(0.6f),
        )
    }
}

private fun localTime(utcTimestamp: String?): String {
    if (utcTimestamp.isNullOrBlank()) return "—"
    return try {
        val instant = Instant.parse("${utcTimestamp}Z")
        val formatter = DateTimeFormatter.ofPattern("MMM d, h:mm a z")
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        "—"
    }
}

private fun buildWorkLabel(state: WorkInfo.State?, runAtMillis: Long?): String {
    if (state == null) return "not scheduled"
    val timeStr = if (runAtMillis != null && runAtMillis > 0) {
        " @ ${SimpleDateFormat("MMM d HH:mm", Locale.getDefault()).format(Date(runAtMillis))}"
    } else ""
    return "${state.name.lowercase()}$timeStr"
}

@Composable
private fun stateColor(state: WorkInfo.State?): androidx.compose.ui.graphics.Color {
    return when (state) {
        WorkInfo.State.ENQUEUED -> MaterialTheme.colorScheme.primary
        WorkInfo.State.RUNNING -> MaterialTheme.colorScheme.tertiary
        WorkInfo.State.SUCCEEDED -> MaterialTheme.colorScheme.onSurfaceVariant
        WorkInfo.State.FAILED, WorkInfo.State.BLOCKED -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }
}
