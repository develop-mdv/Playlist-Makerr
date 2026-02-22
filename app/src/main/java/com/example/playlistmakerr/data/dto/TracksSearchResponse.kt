package com.example.playlistmakerr.data.dto

class TracksSearchResponse(
    val resultCount: Int,
    val results: List<TrackDto>
) : Response()
