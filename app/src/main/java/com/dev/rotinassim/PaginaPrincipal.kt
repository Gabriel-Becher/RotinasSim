package com.dev.rotinassim

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dev.rotinassim.api.RetrofitInstance
import com.dev.rotinassim.api.models.Task
import com.dev.rotinassim.databinding.ActivityPaginaPrincipalBinding
import com.dev.rotinassim.notifications.TaskAlarme
import com.dev.rotinassim.room.DatabaseRoom
import com.dev.rotinassim.room.entities.TaskLocal
import com.dev.rotinassim.utils.CacheUtils
import com.dev.rotinassim.utils.PrefsUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        sincronizarTarefas()
        var telaAtual = R.id.nav_tasks
        alternarTela(TarefasFragment())
        supportFragmentManager.setFragmentResult("syncAtualizado", Bundle())
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_logoff -> { sair(telaAtual); false }
                R.id.nav_tasks -> {
                    if (telaAtual != R.id.nav_tasks) { alternarTela(TarefasFragment()); telaAtual = R.id.nav_tasks }
                    true
                }
                R.id.nav_settings -> {
                    if (telaAtual != R.id.nav_settings) { alternarTela(ConfiguracaoFragment()); telaAtual = R.id.nav_settings }
                    true
                }
                else -> false
            }
        }
    }

    fun sincronizarTarefas() {
        if (userId.isBlank()) { Log.d("Sync", "UserId vazio, cancelando sync"); return }
        lifecycleScope.launch {
            try {
                val db = DatabaseRoom.getDatabase(applicationContext)
                val dao = db.taskLocalDao()
                lifecycleScope.launch {
                    val tasksLocais = withContext(Dispatchers.IO) { dao.buscarTarefasPorUsuarioTodas(userId) }
                    val tasksApi = tasksLocais.map { it.toApi() }
                    RetrofitInstance.INSTANCE.sincronizar(userId, tasksApi)
                        .enqueue(object : Callback<List<Task>> {
                            override fun onResponse(call: Call<List<Task>>, response: Response<List<Task>>) {
                                if (!response.isSuccessful) {
                                    return
                                }
                                val lista = response.body().orEmpty()
                                lifecycleScope.launch{
                                    lista.forEach { t ->
                                        try {
                                            val local = t.toLocal()
                                            dao.upsert(local)
                                            if(!local.deleted && local.notify && local.completedAt == null){
                                                TaskAlarme.schedule(applicationContext, local)
                                            }else{
                                                TaskAlarme.cancel(applicationContext, local.id)
                                            }
                                        } catch (_: Exception) {
                                        }
                                    }
                                    // avisa tela para recarregar
                                    supportFragmentManager.setFragmentResult("syncAtualizado", Bundle())
                                }
                            }
                            override fun onFailure(call: Call<List<Task>>, t: Throwable) {
                                Log.e("Sync", "Falha rede", t)
                            }
                        })
                }
            } catch (e: Exception) {
                Log.e("Sync", "Exceção sync", e)
            }
        }
    }

    private fun TaskLocal.toApi(): Task = Task(
        id = id,
        userId = userId,
        title = title,
        description = description,
        day = day,
        daytime = daytime,
        notify = notify,
        recurring = recurring,
        updatedAt = updatedAt,
        completedAt = completedAt,
        deleted = deleted
    )

    private fun Task.toLocal(): TaskLocal = TaskLocal(
        id = id,
        userId = userId,
        title = title,
        description = description,
        day = day,
        daytime = daytime,
        notify = notify,
        recurring = recurring,
        updatedAt = updatedAt,
        completedAt = completedAt,
        deleted = deleted
    )

    fun alternarTela(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.paginasFragment.id, fragment)
            .commit()
    }

    fun sair(telaAtual: Int) {
        MaterialAlertDialogBuilder(this)
            .setMessage("Deseja sair? Todos os alarmes serão desativados!")
            .setPositiveButton("Sim") { _, _ ->
                PrefsUtils.clearUserId(applicationContext)
                CacheUtils.limparCache(applicationContext)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
                binding.bottomNavigation.selectedItemId = telaAtual
            }
            .show()
    }
}
