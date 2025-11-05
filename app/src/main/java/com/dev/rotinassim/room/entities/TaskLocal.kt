package com.dev.rotinassim.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID


@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = UserLocal::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("id"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class TaskLocal(
    @PrimaryKey(autoGenerate = true) val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val title: String,
    val description: String,
    val day: Date, // dia
    val daytime: String, //hora do dia
    val notify: Boolean,  // se notifica
    val recurring: String
)
