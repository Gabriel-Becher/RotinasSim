package com.dev.rotinassim.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dev.rotinassim.room.entities.UserLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface UserLocalDAO {

    @Insert
    suspend fun criarUsuario(user: UserLocal)

    @Query("SELECT * FROM usuarios WHERE id = :idBusca")
    fun buscarUsuario(idBusca: String): Flow<UserLocal>



}