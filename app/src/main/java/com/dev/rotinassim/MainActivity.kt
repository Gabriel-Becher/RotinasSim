package com.dev.rotinassim

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dev.rotinassim.databinding.ActivityMainBinding
import com.dev.rotinassim.room.DatabaseRoom
import com.dev.rotinassim.room.entities.UserLocal
import com.dev.rotinassim.utils.CacheUtils

class MainActivity : AppCompatActivity() {

    private lateinit var bindind: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        bindind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindind.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val userLogged: UserLocal? = CacheUtils.lerUsuarioCache(this)
        Log.i("USUARIO", userLogged.toString())
        if(userLogged == null){
            mostraLogin()
        }else{
            mostraPaginaInicial()
        }
    }

    fun mostraPaginaInicial(){
        val intent = Intent(this, PaginaPrincipal::class.java)
        startActivity(intent)
        finish()
    }

    fun mostraLogin(){
        supportFragmentManager.beginTransaction().replace(bindind.containerFragmento.id, LoginFragment()).commit()
        bindind.botaoPrincipal.text = "Cadastrar-se"
        bindind.botaoPrincipal.setOnClickListener {
            mostraRegistro()
        }
    }

    fun mostraRegistro(){
        supportFragmentManager.beginTransaction().replace(bindind.containerFragmento.id, RegisterFragment()).commit()
        bindind.botaoPrincipal.text = "Entrar"
        bindind.botaoPrincipal.setOnClickListener {
            mostraLogin()
        }
    }
}