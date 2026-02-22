package com.example.playlistmakerr.data.network

import com.example.playlistmakerr.data.dto.Response
import com.example.playlistmakerr.data.dto.TracksSearchRequest
import java.io.IOException

class RetrofitNetworkClient(private val itunesService: ItunesApi) : NetworkClient {

    override fun doRequest(dto: Any): Response {
        if (dto is TracksSearchRequest) {
            return try {
                val response = itunesService.search(dto.expression).execute()
                val body = response.body()
                if (body != null) {
                    body.apply { resultCode = response.code() }
                } else {
                    Response().apply { resultCode = response.code() }
                }
            } catch (e: IOException) {
                Response().apply { resultCode = -1 }
            }
        }
        return Response().apply { resultCode = 400 }
    }
}
