package com.dev.rotinassim.api

import retrofit2.http.GET

interface ApiService {
    @GET("/")
    suspend fun hello()
}