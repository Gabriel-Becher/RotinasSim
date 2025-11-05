package com.dev.rotinassim.api

import com.dev.rotinassim.api.models.hello
import retrofit2.http.GET

interface ApiService {
    @GET("/")
    suspend fun hello(): hello
}