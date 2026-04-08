package com.kristianolsson.didmyteamwin.worker

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.kristianolsson.didmyteamwin.data.api.RetrofitInstance
import com.kristianolsson.didmyteamwin.data.db.AppDatabase
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

object SchedulerHelper {

    private const val TAG = "SchedulerHelper"
    private val GAME_DURATION_BUFFER = Duration.ofHours(2)

    /**
     * Fetches the next game for [teamId] from the API and schedules a WorkManager
     * job to check the result after the game is expected to end.
     *
     * Returns true if a game was found and scheduled, false otherwise.
     */
    suspend fun scheduleNextGame(context: Context, teamId: String): Boolean {
        val dao = AppDatabase.getInstance(context).trackedTeamDao()

        return try {
            val response = RetrofitInstance.api.getNextEvents(teamId)
            val events = response.events

            if (events.isNullOrEmpty()) {
                Log.i(TAG, "No upcoming games for team $teamId — will poll in 6hrs")
                dao.updateNextEvent(teamId, null, null, null)
                scheduleEventPoll(context, teamId)
                return false
            }

            // Pick the first upcoming event that has no scores and a valid timestamp
            val event = events.firstOrNull { e ->
                e.intHomeScore.isNullOrBlank() && e.intAwayScore.isNullOrBlank()
                    && !e.strTimestamp.isNullOrBlank()
            }

            if (event == null) {
                val reason = if (events.none { it.strTimestamp.isNullOrBlank().not() })
                    "no events have a timestamp" else "all events already have scores"
                Log.i(TAG, "No schedulable event for team $teamId ($reason) — will poll in 6hrs")
                dao.updateNextEvent(teamId, null, null, null)
                scheduleEventPoll(context, teamId)
                return false
            }

            val gameTimeUtc = parseTimestamp(event.strTimestamp!!)
            val checkTime = gameTimeUtc.plus(GAME_DURATION_BUFFER)
            val now = Instant.now()
            val delayMs = Duration.between(now, checkTime).toMillis().coerceAtLeast(60_000) // min 1 minute

            Log.i(TAG, "Scheduling check for ${event.strEvent} at $checkTime (delay: ${delayMs}ms)")

            // Store event info in Room
            dao.updateNextEvent(teamId, event.idEvent, event.strTimestamp, event.strEvent)

            // Enqueue WorkManager job
            val workRequest = OneTimeWorkRequestBuilder<GameCheckWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.HOURS)
                .setInputData(workDataOf("teamId" to teamId))
                .addTag("game_check_$teamId")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "game_check_$teamId",
                ExistingWorkPolicy.REPLACE,
                workRequest,
            )

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule next game for $teamId", e)
            false
        }
    }

    /**
     * Schedule a 24hr poll to re-check for upcoming events.
     * Used when the API has no upcoming games (off-season, between rounds, etc.)
     */
    private fun scheduleEventPoll(context: Context, teamId: String) {
        val workRequest = OneTimeWorkRequestBuilder<NextEventPollWorker>()
            .setInitialDelay(6, TimeUnit.HOURS)
            .setInputData(workDataOf("teamId" to teamId))
            .addTag("event_poll_$teamId")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "event_poll_$teamId",
            ExistingWorkPolicy.REPLACE,
            workRequest,
        )

        Log.i(TAG, "Scheduled 6hr poll for team $teamId")
    }

    /**
     * Cancel any pending game check work for [teamId].
     */
    fun cancelForTeam(context: Context, teamId: String) {
        WorkManager.getInstance(context).cancelUniqueWork("game_check_$teamId")
        WorkManager.getInstance(context).cancelUniqueWork("event_poll_$teamId")
    }

    private fun parseTimestamp(timestamp: String): Instant {
        // TheSportsDB returns "2026-04-01T17:00:00" — UTC, no zone suffix
        return Instant.parse("${timestamp}Z")
    }
}
