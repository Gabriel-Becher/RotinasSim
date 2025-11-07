package com.dev.rotinassim.room.dao

import androidx.room.Dao
import androidx.room.Insert
import com.dev.rotinassim.room.entities.TaskLogLocal


@Dao
interface TaskLogLocalDAO {

    @Insert
    suspend fun inserirLog(log: TaskLogLocal)

}