package com.kristianolsson.didmyteamwin.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackedTeamDao {

    @Query("SELECT * FROM tracked_teams ORDER BY name ASC")
    fun getAllTeams(): Flow<List<TrackedTeam>>

    @Query("SELECT * FROM tracked_teams")
    suspend fun getAllTeamsList(): List<TrackedTeam>

    @Query("SELECT * FROM tracked_teams WHERE idTeam = :id")
    suspend fun getTeam(id: String): TrackedTeam?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(team: TrackedTeam)

    @Query("DELETE FROM tracked_teams WHERE idTeam = :id")
    suspend fun deleteById(id: String)

    @Query("""
        UPDATE tracked_teams 
        SET nextEventId = :eventId, 
            nextEventTimestamp = :timestamp, 
            nextEventName = :eventName, 
            retryCount = 0 
        WHERE idTeam = :teamId
    """)
    suspend fun updateNextEvent(
        teamId: String,
        eventId: String?,
        timestamp: String?,
        eventName: String?,
    )

    @Query("UPDATE tracked_teams SET retryCount = retryCount + 1 WHERE idTeam = :teamId")
    suspend fun incrementRetry(teamId: String)

    @Query("""
        UPDATE tracked_teams 
        SET lastResultSummary = :summary,
            lastResultRevealed = 0,
            lastHomeTeam = :homeTeam,
            lastAwayTeam = :awayTeam,
            lastHomeScore = :homeScore,
            lastAwayScore = :awayScore,
            lastLeague = :league,
            lastDate = :date
        WHERE idTeam = :teamId
    """)
    suspend fun updateLastResult(
        teamId: String,
        summary: String,
        homeTeam: String,
        awayTeam: String,
        homeScore: Int,
        awayScore: Int,
        league: String?,
        date: String?,
    )

    @Query("UPDATE tracked_teams SET lastResultRevealed = 1 WHERE idTeam = :teamId")
    suspend fun markResultRevealed(teamId: String)
}
