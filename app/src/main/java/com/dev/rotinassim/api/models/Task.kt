package com.dev.rotinassim.api.models

import java.util.Date
import java.util.UUID

data class Task(
    val id: String,
    val userId: String,
    val title: String,
    val description: String?,
    val day: Date?, // dia
    val daytime: Int, //hora do dia
    val notify: Boolean,  // se notifica 15 minutos antes
    val recurring: String?,
    val updatedAt: Date
)
