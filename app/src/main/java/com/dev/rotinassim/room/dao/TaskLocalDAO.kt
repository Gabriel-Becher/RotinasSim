package com.dev.rotinassim.room.dao

import androidx.room.Dao
import androidx.room.Insert
import com.dev.rotinassim.room.entities.TaskLocal


@Dao
interface TaskLocalDAO {

    @Insert
    suspend fun criarTarefa(tarefa: TaskLocal)

}