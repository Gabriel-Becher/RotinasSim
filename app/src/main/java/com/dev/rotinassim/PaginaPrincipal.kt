package com.dev.rotinassim

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dev.rotinassim.databinding.ActivityMainBinding
import com.dev.rotinassim.databinding.ActivityPaginaPrincipalBinding
import com.dev.rotinassim.utils.PrefsUtils

class PaginaPrincipal : AppCompatActivity() {

    lateinit var binding: ActivityPaginaPrincipalBinding

    lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPaginaPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        userId = PrefsUtils.getUserId(applicationContext)

        //




    }
}