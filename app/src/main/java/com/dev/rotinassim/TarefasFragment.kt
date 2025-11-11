package com.dev.rotinassim

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.rotinassim.adapters.TarefaAdapter
import com.dev.rotinassim.api.RetrofitInstance
import com.dev.rotinassim.api.models.Task
import com.dev.rotinassim.databinding.FragmentTarefasBinding
import com.dev.rotinassim.notifications.TaskAlarme
import com.dev.rotinassim.room.DatabaseRoom
import com.dev.rotinassim.room.entities.TaskLocal
import com.dev.rotinassim.utils.PrefsUtils
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date

class TarefasFragment : Fragment() {

    private var _binding: FragmentTarefasBinding? = null
    private val binding get() = _binding!!

    private lateinit var proximasAdapter: TarefaAdapter
    private lateinit var recorrentesAdapter: TarefaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTarefasBinding.inflate(inflater, container, false)
        configurarRecyclerViews()
        binding.botaoCriarTarefa.setOnClickListener {
            AdicionarTarefaFragment().show(parentFragmentManager, "")
        }

        parentFragmentManager.setFragmentResultListener("novaTarefa", viewLifecycleOwner) { _, _ ->
            carregarTarefas()
        }
        parentFragmentManager.setFragmentResultListener("syncAtualizado", viewLifecycleOwner) { _, _ ->
            carregarTarefas()
        }

        carregarTarefas()
        return binding.root
    }

    private fun configurarRecyclerViews() = with(binding) {
        proximasAdapter = TarefaAdapter(mutableListOf(), onConcluir = { concluirTarefa(it) }, onExcluir = { excluirTarefa(it) })
        recorrentesAdapter = TarefaAdapter(mutableListOf(), onConcluir = { concluirTarefa(it) }, onExcluir = { excluirTarefa(it) })

        listaProximas.layoutManager = LinearLayoutManager(requireContext())
        listaProximas.adapter = proximasAdapter

        listaRecorrente.layoutManager = LinearLayoutManager(requireContext())
        listaRecorrente.adapter = recorrentesAdapter
    }

    private fun carregarTarefas() {
        val userId = PrefsUtils.getUserId(requireContext())
        if (userId.isBlank()) return

        lifecycleScope.launch {
            try {
                val dao = DatabaseRoom.getDatabase(requireContext()).taskLocalDao()
                val todas = dao.buscarTarefasPorUsuario(userId)

                val recorrentes = todas.filter { val r = it.recurring; !r.isNullOrBlank() && r.contains('1') }
                val proximas = todas.filter { val r = it.recurring; r.isNullOrBlank() || !r.contains('1') }

                proximasAdapter.atualizarLista(proximas.sortedBy { it.day ?: Long.MIN_VALUE;  }.sortedBy { it.completedAt ?: Long.MIN_VALUE })
                recorrentesAdapter.atualizarLista(recorrentes.sortedBy { it.daytime }.sortedBy { it.completedAt ?: Long.MIN_VALUE })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun concluirTarefa(task: TaskLocal) {
        lifecycleScope.launch {
            val db = DatabaseRoom.getDatabase(requireContext())
            val dao = db.taskLocalDao()
            try {
                val agora = Date().time
                val atualizada = task.copy(completedAt = agora)
                dao.atualizarTarefa(atualizada)
                // Cancela alarme associado
                TaskAlarme.cancel(requireContext(), task.id)
                carregarTarefas()

                // Tenta sincronizar
                val taskApi = Task(
                    id = atualizada.id,
                    userId = atualizada.userId,
                    title = atualizada.title,
                    description = atualizada.description,
                    day = atualizada.day,
                    daytime = atualizada.daytime,
                    notify = atualizada.notify,
                    recurring = atualizada.recurring,
                    updatedAt = Date().time,
                    completedAt = atualizada.completedAt
                )
                RetrofitInstance.INSTANCE.atualizarTarefa(atualizada.id, taskApi).enqueue(object: Callback<Task> {
                    override fun onResponse(call: Call<Task>, response: Response<Task>) { }
                    override fun onFailure(call: Call<Task>, t: Throwable) { }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun excluirTarefa(task: TaskLocal) {
        lifecycleScope.launch {
            val db = DatabaseRoom.getDatabase(requireContext())
            val dao = db.taskLocalDao()
            try {
                RetrofitInstance.INSTANCE.deletarTarefa(task.id).enqueue(object: Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {}
                    override fun onFailure(call: Call<Void>, t: Throwable) {}
                })
                lifecycleScope.launch {
                    dao.atualizarTarefa(task.copy(deleted = true))
                    carregarTarefas()
                }

            } catch (_: Exception) {
                dao.atualizarTarefa(task.copy(deleted = true))
                carregarTarefas()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? PaginaPrincipal)?.sincronizarTarefas()
        carregarTarefas()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? PaginaPrincipal)?.sincronizarTarefas()
        _binding = null
    }
}