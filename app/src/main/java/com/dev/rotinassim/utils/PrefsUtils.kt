package com.dev.rotinassim.utils

import android.content.Context
import android.util.Log
import androidx.core.content.edit

object PrefsUtils {

    private val PREFS_NAME = "userInfo"
    private const val KEY_USER = "userId"
    private const val KEY_CONFIG = "notiTime"

    fun getUserId(context: Context): String {

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs == null){
            return ""
        }
        val userId = prefs.getString(KEY_USER, "")
        if (userId != null){
            return userId
        }
        return ""
    }

    fun setUserId(context: Context, userId: String){
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_USER, userId)
        }
    }

    fun clearUserId(context: Context){
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { remove(KEY_USER) }
    }

    fun getNotiTime(context: Context): Int{
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs == null){
            return 15
        }
        val notiTime = prefs.getInt(KEY_CONFIG, 15)
        Log.i("Valor carregado", notiTime.toString())
        return notiTime
    }

    fun setNotiTime(context: Context, notiTime: Int){
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putInt(KEY_CONFIG, notiTime)
            Log.i("Valor salvo Noti", notiTime.toString())
        }
    }
}