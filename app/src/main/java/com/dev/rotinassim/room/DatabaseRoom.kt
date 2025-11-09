package com.dev.rotinassim.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dev.rotinassim.room.dao.TaskLocalDAO
import com.dev.rotinassim.room.dao.TaskLogLocalDAO
import com.dev.rotinassim.room.dao.UserLocalDAO
import com.dev.rotinassim.room.entities.TaskLocal
import com.dev.rotinassim.room.entities.TaskLogLocal
import com.dev.rotinassim.room.entities.UserLocal
import com.dev.rotinassim.room.typerconverters.DateConverters

@Database(entities = [TaskLocal::class, TaskLogLocal::class, UserLocal::class], version = 1)
@TypeConverters(DateConverters::class)
abstract class DatabaseRoom: RoomDatabase() {
    abstract fun userLocalDao(): UserLocalDAO
    abstract fun taskLocalDao(): TaskLocalDAO
    abstract fun taskLogLocalDao(): TaskLogLocalDAO

    companion object {
        @Volatile private var INSTANCE: DatabaseRoom? = null
        fun getDatabase(context: Context): DatabaseRoom {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DatabaseRoom::class.java,
                    "tudoLocal.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }

}