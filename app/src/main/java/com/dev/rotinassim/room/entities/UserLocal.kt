package com.dev.rotinassim.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID


@Entity(tableName = "usuarios")
data class UserLocal(
    @PrimaryKey(autoGenerate = false) val id: String,
    val email: String,
    val password: String,
)
