package com.example.playlistmakerr.data.network

import com.example.playlistmakerr.data.dto.TracksSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApi {
    @GET("/search?entity=song")
    suspend fun search(@Query("term") text: String): Response<TracksSearchResponse>
}
