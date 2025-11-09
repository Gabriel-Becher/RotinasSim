package com.dev.rotinassim.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dev.rotinassim.room.entities.UserLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface UserLocalDAO {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun criarUsuario(user: UserLocal)

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserLocal?

}