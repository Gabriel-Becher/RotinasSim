package com.dev.rotinassim.api.models

import java.util.Date
import java.util.UUID

data class TaskLog(
    val id: String,
    val taskId: String,
    val userId: String,
    val completedAt: Date,
    val status: Int
)
