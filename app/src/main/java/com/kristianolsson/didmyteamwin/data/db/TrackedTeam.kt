package com.kristianolsson.didmyteamwin.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracked_teams")
data class TrackedTeam(
    @PrimaryKey val idTeam: String,
    val name: String,
    val sport: String? = null,
    val league: String? = null,
    val country: String? = null,
    val badgeUrl: String? = null,
    // Scheduling state
    val nextEventId: String? = null,
    val nextEventTimestamp: String? = null,
    val nextEventName: String? = null,
    val retryCount: Int = 0,
    // Last result
    val lastResultSummary: String? = null,
    val lastResultRevealed: Boolean = false,
    val lastHomeTeam: String? = null,
    val lastAwayTeam: String? = null,
    val lastHomeScore: Int? = null,
    val lastAwayScore: Int? = null,
    val lastLeague: String? = null,
    val lastDate: String? = null,
)
