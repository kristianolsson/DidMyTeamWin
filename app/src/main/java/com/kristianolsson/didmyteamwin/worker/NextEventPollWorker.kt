package com.kristianolsson.didmyteamwin.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Lightweight worker that just re-attempts to find and schedule the next game.
 * Used when eventsnext returns no upcoming games (off-season, between rounds, etc.)
 * — retries every 24 hours until a game is found.
 */
class NextEventPollWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "NextEventPollWorker"
    }

    override suspend fun doWork(): Result {
        val teamId = inputData.getString("teamId")
            ?: return Result.failure()

        Log.i(TAG, "Polling for next event for team $teamId")

        val scheduled = SchedulerHelper.scheduleNextGame(applicationContext, teamId)
        if (scheduled) {
            Log.i(TAG, "Found and scheduled next game for team $teamId")
        } else {
            Log.i(TAG, "Still no upcoming games for team $teamId — will poll again in 24hrs")
        }

        // Always succeed — scheduleNextGame handles re-polling internally
        return Result.success()
    }
}
