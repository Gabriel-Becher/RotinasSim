package com.dev.rotinassim.api.models

import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
    val password: String,
    val notificationtime: Int = 0
)
