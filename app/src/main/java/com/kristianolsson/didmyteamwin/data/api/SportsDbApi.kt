package com.kristianolsson.didmyteamwin.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface SportsDbApi {

    @GET("searchteams.php")
    suspend fun searchTeams(@Query("t") teamName: String): TeamSearchResponse

    @GET("eventsnext.php")
    suspend fun getNextEvents(@Query("id") teamId: String): EventsResponse

    @GET("lookupevent.php")
    suspend fun lookupEvent(@Query("id") eventId: String): EventsResponse
}
