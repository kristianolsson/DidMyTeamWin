package com.kristianolsson.didmyteamwin.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kristianolsson.didmyteamwin.data.api.RetrofitInstance
import com.kristianolsson.didmyteamwin.data.db.AppDatabase
import com.kristianolsson.didmyteamwin.notification.NotificationHelper

class GameCheckWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "GameCheckWorker"
        private const val MAX_RETRIES = 12
    }

    override suspend fun doWork(): Result {
        val teamId = inputData.getString("teamId")
            ?: return Result.failure()

        val dao = AppDatabase.getInstance(applicationContext).trackedTeamDao()
        val team = dao.getTeam(teamId) ?: return Result.failure()
        val eventId = team.nextEventId ?: return Result.failure()

        Log.i(TAG, "Checking result for ${team.name}, event $eventId (retry ${team.retryCount})")

        return try {
            val response = RetrofitInstance.api.lookupEvent(eventId)
            val event = response.events?.firstOrNull()

            if (event == null) {
                Log.w(TAG, "Event $eventId not found in API response")
                handleError(team.name, teamId, "Could not find game data")
                return Result.failure()
            }

            val status = event.strStatus?.uppercase() ?: ""
            val postponed = event.strPostponed?.lowercase() == "yes"
            val hasScores = !event.intHomeScore.isNullOrBlank() && !event.intAwayScore.isNullOrBlank()
            // TheSportsDB returns "Match Finished" for completed games (all competitions)
            val finishedStatuses = setOf("FT", "AET", "PEN", "MATCH FINISHED")
            // "Not Started" is what TheSportsDB returns for upcoming games
            val liveStatuses = setOf("NOT STARTED", "NS", "LIVE", "HT", "1H", "2H", "ET", "BT", "Q1", "Q2", "Q3", "Q4", "OT", "INT", "SUSP")
            val isFinished = status in finishedStatuses
            val isLive = !isFinished && (status in liveStatuses || hasScores)

            Log.i(TAG, "Event $eventId status='$status' scores=${event.intHomeScore}-${event.intAwayScore} isFinished=$isFinished isLive=$isLive")

            when {
                // Game finished
                isFinished -> {
                    val homeScore = event.intHomeScore?.toIntOrNull() ?: 0
                    val awayScore = event.intAwayScore?.toIntOrNull() ?: 0
                    val summary = buildSummary(
                        team.name, teamId,
                        event.strHomeTeam, event.strAwayTeam,
                        homeScore, awayScore,
                        event.strLeague,
                    )

                    dao.updateLastResult(
                        teamId = teamId,
                        summary = summary,
                        homeTeam = event.strHomeTeam,
                        awayTeam = event.strAwayTeam,
                        homeScore = homeScore,
                        awayScore = awayScore,
                        league = event.strLeague,
                        date = event.dateEvent,
                    )

                    NotificationHelper.showResultNotification(
                        applicationContext, teamId, team.name,
                    )

                    // Schedule next game (don't let failures here trigger error notification)
                    try {
                        SchedulerHelper.scheduleNextGame(applicationContext, teamId)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to schedule next game after result for ${team.name}", e)
                    }
                    Result.success()
                }

                // Game postponed or cancelled — only if not currently live/in-progress
                !isLive && (postponed || status == "PST" || status == "CANC" || status == "ABD") -> {
                    NotificationHelper.showCancelledNotification(
                        applicationContext, teamId, team.name,
                    )
                    dao.updateNextEvent(teamId, null, null, null)
                    try {
                        SchedulerHelper.scheduleNextGame(applicationContext, teamId)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to schedule next game after cancellation for ${team.name}", e)
                    }
                    Result.success()
                }

                // Game not finished yet — retry
                else -> {
                    if (team.retryCount >= MAX_RETRIES) {
                        Log.w(TAG, "Max retries reached for ${team.name}")
                        NotificationHelper.showErrorNotification(
                            applicationContext, teamId, team.name,
                            "Could not get result after ${MAX_RETRIES}hrs — game may still be in progress",
                        )
                        dao.updateNextEvent(teamId, null, null, null)
                        try {
                            SchedulerHelper.scheduleNextGame(applicationContext, teamId)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to schedule next game after max retries for ${team.name}", e)
                        }
                        Result.success()
                    } else {
                        dao.incrementRetry(teamId)
                        Log.i(TAG, "Game not finished, retrying in ~1hr (attempt ${team.retryCount + 1}/$MAX_RETRIES)")
                        Result.retry()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking result for ${team.name}", e)
            handleError(team.name, teamId, "Error fetching game result")
            Result.failure()
        }
    }

    private suspend fun handleError(teamName: String, teamId: String, message: String) {
        NotificationHelper.showErrorNotification(applicationContext, teamId, teamName, message)
        val dao = AppDatabase.getInstance(applicationContext).trackedTeamDao()
        dao.updateNextEvent(teamId, null, null, null)
        // Still try to schedule the next game
        try {
            SchedulerHelper.scheduleNextGame(applicationContext, teamId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule next game after error for $teamName", e)
        }
    }

    private fun buildSummary(
        trackedName: String,
        trackedId: String,
        homeTeam: String,
        awayTeam: String,
        homeScore: Int,
        awayScore: Int,
        league: String?,
    ): String {
        val isHome = homeTeam.contains(trackedName, ignoreCase = true)
        val opponent = if (isHome) awayTeam else homeTeam
        val teamScore = if (isHome) homeScore else awayScore
        val opponentScore = if (isHome) awayScore else homeScore
        val leagueSuffix = if (league != null) " in $league" else ""

        val isUs = java.util.Locale.getDefault().country == "US"
        val scoreText = if (isUs) {
            // US: winning score first
            if (teamScore >= opponentScore) "$teamScore-$opponentScore"
            else "$opponentScore-$teamScore"
        } else {
            // EU: home score first
            "$homeScore-$awayScore"
        }

        return when {
            teamScore > opponentScore -> "$trackedName beat $opponent $scoreText$leagueSuffix"
            teamScore < opponentScore -> "$trackedName lost to $opponent $scoreText$leagueSuffix"
            else -> "$trackedName drew with $opponent $scoreText$leagueSuffix"
        }
    }
}
