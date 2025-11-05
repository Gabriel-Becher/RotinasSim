package com.dev.rotinassim.api.models

import java.util.Date
import java.util.UUID

data class TaskLog(
    val id: UUID,
    val taskId: UUID,
    val userId: UUID,
    val completedAt: Date,
    val status: Int
)
