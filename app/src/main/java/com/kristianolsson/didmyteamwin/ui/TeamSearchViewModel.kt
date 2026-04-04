package com.kristianolsson.didmyteamwin.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kristianolsson.didmyteamwin.data.api.RetrofitInstance
import com.kristianolsson.didmyteamwin.data.api.Team
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

class TeamSearchViewModel(application: Application) : AndroidViewModel(application) {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _results = MutableStateFlow<List<Team>>(emptyList())
    val results: StateFlow<List<Team>> = _results

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        @OptIn(FlowPreview::class)
        viewModelScope.launch {
            _query
                .debounce(500)
                .filter { it.trim().length >= 2 }
                .collectLatest { searchTerm ->
                    search(searchTerm)
                }
        }
    }

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
        if (newQuery.trim().length < 2) {
            _results.value = emptyList()
            _error.value = null
        }
    }

    private suspend fun search(term: String) {
        val trimmed = term.trim()
        if (trimmed.length < 2) return
        _isLoading.value = true
        _error.value = null
        try {
            val response = RetrofitInstance.api.searchTeams(trimmed)
            _results.value = response.teams ?: emptyList()
        } catch (e: Exception) {
            _error.value = "Search failed: ${e.message}"
            _results.value = emptyList()
        } finally {
            _isLoading.value = false
        }
    }
}
