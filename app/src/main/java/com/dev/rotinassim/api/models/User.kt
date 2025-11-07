package com.dev.rotinassim.api.models

import java.util.UUID

data class User(
    val id: String?,
    val email: String,
    val password: String,
)
