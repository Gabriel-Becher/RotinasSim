package com.dev.rotinassim.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID


@Entity(tableName = "usuarios")
data class UserLocal(
    @PrimaryKey(autoGenerate = true) val id: String = UUID.randomUUID().toString(),
    val senha: String,
    val email: String,
    val notificationtime: Int = 0
)
