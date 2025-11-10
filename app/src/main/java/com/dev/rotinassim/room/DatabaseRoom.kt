package com.dev.rotinassim.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dev.rotinassim.room.dao.TaskLocalDAO
import com.dev.rotinassim.room.dao.UserLocalDAO
import com.dev.rotinassim.room.entities.TaskLocal
import com.dev.rotinassim.room.entities.UserLocal

@Database(entities = [TaskLocal::class, UserLocal::class], version = 2)
abstract class DatabaseRoom: RoomDatabase() {
    abstract fun userLocalDao(): UserLocalDAO
    abstract fun taskLocalDao(): TaskLocalDAO

    companion object {
        @Volatile private var INSTANCE: DatabaseRoom? = null
        fun getDatabase(context: Context): DatabaseRoom {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DatabaseRoom::class.java,
                    "tudoLocal.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }

}