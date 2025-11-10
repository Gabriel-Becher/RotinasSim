package com.dev.rotinassim.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dev.rotinassim.room.entities.TaskLocal


@Dao
interface TaskLocalDAO {

    @Insert
    suspend fun criarTarefa(tarefa: TaskLocal)

    @Update
    suspend fun atualizarTarefa(tarefa: TaskLocal)

    @Delete
    suspend fun deletarTarefa(tarefa: TaskLocal)

    @Query("SELECT * FROM tasks WHERE userId = :userId AND deleted = 0")
    suspend fun buscarTarefasPorUsuario(userId: String): List<TaskLocal>

    @Query("SELECT * FROM tasks WHERE userId = :userId")
    suspend fun buscarTarefasPorUsuarioTodas(userId: String): List<TaskLocal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(tarefa: TaskLocal)
}