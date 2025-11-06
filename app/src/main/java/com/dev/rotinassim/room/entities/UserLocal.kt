package com.dev.rotinassim.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID


@Entity(tableName = "usuarios")
data class UserLocal(
    @PrimaryKey(autoGenerate = false) val id: String = UUID.randomUUID().toString(),
    val email: String,
    val senha: String,
    val notificationtime: Int = 0,
    val isSynced: Boolean = false
)
