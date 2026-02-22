package com.example.playlistmakerr.data.network

import com.example.playlistmakerr.data.dto.Response

interface NetworkClient {
    fun doRequest(dto: Any): Response
}
