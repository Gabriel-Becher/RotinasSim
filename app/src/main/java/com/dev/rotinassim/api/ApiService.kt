package com.dev.rotinassim.api

import com.dev.rotinassim.api.models.Task
import com.dev.rotinassim.api.models.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @POST("/users")
    fun criarUsuario(@Body userdata: User): Call<User>

    @POST("/users/login")
    fun login(@Body userdata: User): Call<User>

    @POST("/tasks")
    fun criarTarefa(@Body task: Task): Call<Task>

    @PUT("/tasks/{id}")
    fun atualizarTarefa(@Path("id") id: String, @Body task: Task): Call<Task>

    @DELETE("/tasks/{id}")
    fun deletarTarefa(@Path("id") id: String): Call<Void>

    @POST("/sync/tasks/{userId}")
    fun sincronizar(@Path("userId") userId: String, @Body tasks: List<Task>): Call<List<Task>>
}