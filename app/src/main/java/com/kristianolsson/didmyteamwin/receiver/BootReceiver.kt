package com.kristianolsson.didmyteamwin.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kristianolsson.didmyteamwin.data.db.AppDatabase
import com.kristianolsson.didmyteamwin.worker.SchedulerHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        Log.i(TAG, "Boot completed — rescheduling game checks")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.getInstance(context).trackedTeamDao()
                val teams = dao.getAllTeamsList()

                for (team in teams) {
                    if (team.nextEventId != null) {
                        Log.i(TAG, "Rescheduling check for ${team.name}")
                        SchedulerHelper.scheduleNextGame(context, team.idTeam)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error rescheduling after boot", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
