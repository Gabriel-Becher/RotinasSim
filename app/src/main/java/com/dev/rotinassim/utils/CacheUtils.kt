package com.dev.rotinassim.utils

import android.content.Context
import android.util.Log
import com.dev.rotinassim.room.entities.UserLocal
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.Exception

object CacheUtils {

    private const val CACHE_FILE= "user_data.txt"

    fun lerUsuarioCache(context: Context): UserLocal? {
        var usuario: UserLocal? = null
        try {
            val file = File(context.cacheDir, CACHE_FILE)
            Log.i("Cache existe ao ler:", file.exists().toString())
            FileReader(file).use {
                reader ->
                BufferedReader(reader).use {
                    bufferedReader ->
                        val linha = bufferedReader.readText()
                        Log.i("CACHE LIDO", linha)
                        val lista = linha.split(";")
                        usuario = UserLocal(lista[0], lista[1], lista[2])
                }
            }
            Log.i("LIDO", usuario.toString())
            return usuario
        }catch (e: kotlin.Exception){
            Log.e("Erro de arquivo", e.message.toString())
            return null
        }
    }

    fun escreverUsuarioCache(context: Context, localUserData: UserLocal): Int {

        val texto = "${localUserData.id};${localUserData.email};${localUserData.password}"

        try {
            val file = File(context.cacheDir, CACHE_FILE)
            FileWriter(file).use { writer ->
                 writer.write(texto)
                Log.i("ESCRITO", texto)
                Log.i("CACHE EXISTE", file.exists().toString())
            }
        }catch (e: Exception){
            Log.e("Erro de arquivo", e.message.toString())
            return 0
        }
        return 1
    }

    fun limparCache(context: Context){
        try {
            val file = File(context.cacheDir, CACHE_FILE)
            FileWriter(file).use {
                writer -> writer.write("")
                writer.close()
            }
        }catch (e: kotlin.Exception){
            Log.e("Erro", e.message.toString())
        }
    }

}