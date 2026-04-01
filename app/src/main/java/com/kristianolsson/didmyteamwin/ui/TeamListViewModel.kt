package com.kristianolsson.didmyteamwin.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kristianolsson.didmyteamwin.data.api.RetrofitInstance
import com.kristianolsson.didmyteamwin.data.api.Team
import com.kristianolsson.didmyteamwin.data.db.AppDatabase
import com.kristianolsson.didmyteamwin.data.db.TrackedTeam
import com.kristianolsson.didmyteamwin.worker.SchedulerHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TeamListViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).trackedTeamDao()

    val teams: StateFlow<List<TrackedTeam>> = dao.getAllTeams()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTeam(team: Team) {
        viewModelScope.launch {
            val trackedTeam = TrackedTeam(
                idTeam = team.idTeam,
                name = team.strTeam,
                sport = team.strSport,
                league = team.strLeague,
                country = team.strCountry,
                badgeUrl = team.strBadge,
            )
            dao.upsert(trackedTeam)
            SchedulerHelper.scheduleNextGame(getApplication(), team.idTeam)
        }
    }

    fun removeTeam(teamId: String) {
        viewModelScope.launch {
            dao.deleteById(teamId)
            SchedulerHelper.cancelForTeam(getApplication(), teamId)
        }
    }

    fun revealResult(teamId: String) {
        viewModelScope.launch {
            dao.markResultRevealed(teamId)
        }
    }
}
