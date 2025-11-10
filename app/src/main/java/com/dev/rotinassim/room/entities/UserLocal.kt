package com.dev.rotinassim.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID


@Entity(tableName = "usuarios",
    indices = [Index(value = ["email"], unique = true)])
data class UserLocal(
    @PrimaryKey(autoGenerate = false) val id: String,
    @ColumnInfo(name = "email")
    val email: String,
    val password: String,
)
