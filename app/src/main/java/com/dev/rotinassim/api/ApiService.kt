package com.dev.rotinassim.api

import com.dev.rotinassim.api.models.User
import com.dev.rotinassim.room.entities.UserLocal
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("/users")
    fun criarUsuario(@Body userdata: User): Call<User>

}