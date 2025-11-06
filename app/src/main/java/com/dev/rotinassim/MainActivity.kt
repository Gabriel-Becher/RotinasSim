package com.dev.rotinassim

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dev.rotinassim.databinding.ActivityMainBinding
import com.dev.rotinassim.room.DatabaseRoom

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

        if(savedInstanceState == null){
            mostraLogin()
        }
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