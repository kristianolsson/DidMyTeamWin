package com.kristianolsson.didmyteamwin.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kristianolsson.didmyteamwin.R
import com.kristianolsson.didmyteamwin.data.db.TrackedTeam
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamListScreen(
    viewModel: TeamListViewModel,
    onAddTeam: () -> Unit,
    onTeamClick: (String) -> Unit,
) {
    val teams by viewModel.teams.collectAsState()
    val bgColor = MaterialTheme.colorScheme.background

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (teams.isNotEmpty()) {
                FloatingActionButton(
                    onClick = onAddTeam,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add team")
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor),
        ) {
            // Header — banner image with text baked in
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(R.drawable.banner),
                    contentDescription = "Did My Team Win banner",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth,
                )
                // Small gradient at bottom edge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, bgColor),
                            )
                        )
                )
            }

            // Divider between header and content
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 0.5.dp,
            )

            // Content zone — darker area
            if (teams.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 32.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(16.dp))

                    WelcomeItem(
                        emoji = "\uD83C\uDFC6",
                        title = "Track your teams",
                        description = "Search and add teams you follow",
                    )
                    Spacer(Modifier.height(20.dp))
                    WelcomeItem(
                        emoji = "\uD83D\uDD15",
                        title = "Spoiler-free results",
                        description = "Get notified when a game ends — scores are hidden until you're ready",
                    )
                    Spacer(Modifier.height(20.dp))
                    WelcomeItem(
                        emoji = "\uD83D\uDCC5",
                        title = "Automatic scheduling",
                        description = "The app monitors upcoming games and checks results for you",
                    )
                    Spacer(Modifier.height(20.dp))
                    WelcomeItem(
                        emoji = "\uD83D\uDD12",
                        title = "Runs completely local",
                        description = "No account needed. Your data stays on your device.",
                    )

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = onAddTeam,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add a team", style = MaterialTheme.typography.titleSmall)
                    }

                    Spacer(Modifier.height(24.dp))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 12.dp,
                        bottom = 12.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(teams, key = { it.idTeam }) { team ->
                        TeamCard(
                            team = team,
                            onClick = { onTeamClick(team.idTeam) },
                            onDelete = { viewModel.removeTeam(team.idTeam) },
                        )
                    }
                }
            }

            // Persistent footer
            val uriHandler = LocalUriHandler.current
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { uriHandler.openUri("https://www.thesportsdb.com") }
                    .padding(bottom = padding.calculateBottomPadding() + 12.dp, top = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Powered by TheSportsDB",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }
        }
    }
}

@Composable
private fun TeamCard(
    team: TrackedTeam,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Team badge
            AsyncImage(
                model = team.badgeUrl,
                contentDescription = "${team.name} badge",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Fit,
            )

            Spacer(Modifier.width(16.dp))

            // Team info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = team.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                val subtitle = listOfNotNull(team.sport, team.league)
                    .joinToString(" · ")
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Status line
                when {
                    team.lastResultSummary != null && !team.lastResultRevealed -> {
                        Text(
                            text = "🔔 Result available — tap to reveal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                    team.lastResultSummary != null && team.lastResultRevealed -> {
                        Text(
                            text = team.lastResultSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    team.nextEventName != null -> {
                        val checkTimeText = team.nextEventTimestamp?.let {
                            formatCheckTime(it)
                        }
                        Text(
                            text = if (checkTimeText != null)
                                "⏳ ${team.nextEventName} · checking $checkTimeText"
                            else
                                "⏳ Next: ${team.nextEventName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    else -> {
                        Text(
                            text = "🔍 Checking for next game...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove ${team.name}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun WelcomeItem(
    emoji: String,
    title: String,
    description: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = emoji,
            fontSize = 28.sp,
            modifier = Modifier.padding(end = 16.dp, top = 2.dp),
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatCheckTime(gameTimestamp: String): String? {
    return try {
        val gameTime = Instant.parse("${gameTimestamp}Z")
        val checkTime = gameTime.plus(Duration.ofHours(2))
        val now = Instant.now()
        val hoursUntil = Duration.between(now, checkTime).toHours()

        when {
            checkTime.isBefore(now) -> "soon"
            hoursUntil < 1 -> "in <1 hour"
            hoursUntil < 24 -> "in ${hoursUntil}h"
            else -> {
                val formatter = DateTimeFormatter.ofPattern("MMM d 'at' h:mm a")
                    .withZone(ZoneId.systemDefault())
                formatter.format(checkTime)
            }
        }
    } catch (e: Exception) {
        null
    }
}
