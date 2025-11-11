package com.dev.rotinassim.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID


@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = UserLocal::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("userId"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class TaskLocal(
    @PrimaryKey(autoGenerate = false) val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val title: String,
    val description: String?,
    val day: Long?,
    val daytime: Long,
    val notify: Boolean,
    val recurring: String,
    val updatedAt: Long,
    val completedAt: Long?,
    val deleted: Boolean = false
)
