package com.dev.rotinassim.api

import com.dev.rotinassim.room.entities.UserLocal
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("/users/")
    fun criarUsuario(userdata: UserLocal): Call<UserLocal>


}