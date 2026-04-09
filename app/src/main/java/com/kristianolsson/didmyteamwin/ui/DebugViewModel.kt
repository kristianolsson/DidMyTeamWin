package com.kristianolsson.didmyteamwin.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.kristianolsson.didmyteamwin.data.db.AppDatabase
import com.kristianolsson.didmyteamwin.data.db.TrackedTeam
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TeamDebugInfo(
    val team: TrackedTeam,
    val gameCheckStatus: WorkInfo.State?,
    val gameCheckRunAtMillis: Long?,
    val eventPollStatus: WorkInfo.State?,
    val eventPollRunAtMillis: Long?,
)

class DebugViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).trackedTeamDao()
    private val workManager = WorkManager.getInstance(application)

    private val _items = MutableStateFlow<List<TeamDebugInfo>>(emptyList())
    val items: StateFlow<List<TeamDebugInfo>> = _items

    private val _lastRefresh = MutableStateFlow(0L)
    val lastRefresh: StateFlow<Long> = _lastRefresh

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val teams = dao.getAllTeamsList()
            _items.value = teams.map { team ->
                val gameCheck = workManager
                    .getWorkInfosForUniqueWork("game_check_${team.idTeam}")
                    .get()
                    .firstOrNull()
                val eventPoll = workManager
                    .getWorkInfosForUniqueWork("event_poll_${team.idTeam}")
                    .get()
                    .firstOrNull()

                TeamDebugInfo(
                    team = team,
                    gameCheckStatus = gameCheck?.state,
                    gameCheckRunAtMillis = gameCheck?.nextScheduleTimeMillis,
                    eventPollStatus = eventPoll?.state,
                    eventPollRunAtMillis = eventPoll?.nextScheduleTimeMillis,
                )
            }
            _lastRefresh.value = System.currentTimeMillis()
        }
    }
}
