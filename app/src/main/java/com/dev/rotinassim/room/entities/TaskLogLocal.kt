package com.dev.rotinassim.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(
    tableName = "tasklogs",
    foreignKeys = [
        ForeignKey(
            entity = UserLocal::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("id"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TaskLocal::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("id"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)

data class TaskLogLocal(
    @PrimaryKey(autoGenerate = false) val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val userId: String,
    val completedAt: Long,
)
