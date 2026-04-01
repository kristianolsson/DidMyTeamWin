package com.kristianolsson.didmyteamwin.data.api

import com.google.gson.annotations.SerializedName

// -- Team Search --

data class TeamSearchResponse(
    val teams: List<Team>?
)

data class Team(
    val idTeam: String,
    val strTeam: String,
    val strTeamAlternate: String?,
    val strSport: String?,
    val strLeague: String?,
    val strCountry: String?,
    val strBadge: String?,
    val strStadium: String?,
)

// -- Events --

data class EventsResponse(
    val events: List<Event>?
)

data class Event(
    val idEvent: String,
    val strEvent: String,
    val strHomeTeam: String,
    val strAwayTeam: String,
    val intHomeScore: String?,
    val intAwayScore: String?,
    val strLeague: String?,
    val dateEvent: String?,
    val strTimestamp: String,
    val strStatus: String?,
    val strPostponed: String?,
    val idHomeTeam: String?,
    val idAwayTeam: String?,
    val strHomeTeamBadge: String?,
    val strAwayTeamBadge: String?,
    val strSport: String?,
)
