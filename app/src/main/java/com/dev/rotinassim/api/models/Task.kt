package com.dev.rotinassim.api.models

data class Task(
    val id: String,
    val userId: String,
    val title: String,
    val description: String?,
    val day: Long?, // dia
    val daytime: Long, //hora do dia
    val notify: Boolean,
    val recurring: String?,
    val updatedAt: Long,
    val completedAt: Long?,
    val deleted: Boolean = false
)
