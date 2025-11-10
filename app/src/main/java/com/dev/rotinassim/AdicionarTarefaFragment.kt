package com.dev.rotinassim

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.dev.rotinassim.api.RetrofitInstance
import com.dev.rotinassim.api.models.Task
import com.dev.rotinassim.databinding.FragmentAdicionarTarefaBinding
import com.dev.rotinassim.notifications.TaskAlarme
import com.dev.rotinassim.room.DatabaseRoom
import com.dev.rotinassim.room.entities.TaskLocal
import com.dev.rotinassim.utils.PrefsUtils
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class AdicionarTarefaFragment : DialogFragment() {

    private var _binding: FragmentAdicionarTarefaBinding? = null
    private val binding get() = _binding!!

    lateinit var userId: String

    private var diaSelecionadoLong: Long? = null
    private var horaSelecionada: Int? = null
    private var minutoSelecionado: Int? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentAdicionarTarefaBinding.inflate(layoutInflater)
        val view = binding.root

        // Configurar alementos de data e hora
        binding.inputDia.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecione o dia")
                .build()
            datePicker.addOnPositiveButtonClickListener { selection ->
                diaSelecionadoLong = selection
                binding.inputDia.setText(datePicker.headerText)
            }
            datePicker.show(parentFragmentManager, "date_picker")
        }

        binding.inputHora.setOnClickListener {
            val timePicker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Selecione o horário")
                .build()
            timePicker.addOnPositiveButtonClickListener {
                horaSelecionada = timePicker.hour
                minutoSelecionado = timePicker.minute
                val mm = (minutoSelecionado ?: 0).toString().padStart(2,'0')
                val hh = (horaSelecionada ?: 0).toString().padStart(2,'0')
                binding.inputHora.setText("$hh:$mm")
            }
            timePicker.show(parentFragmentManager, "time_picker")
        }

        userId= PrefsUtils.getUserId(requireContext())

        configurarEventos()

        return AlertDialog.Builder(requireContext())//essa função cria meu dialogo**
            .setView(view)
            .create()
    }

    private fun configurarEventos() = with(binding) {
        botaoCriar.visibility = View.VISIBLE
        botaoAtualizar.visibility = View.GONE
        botaoExcluir.visibility = View.GONE

        botaoCriar.setOnClickListener {
            val tarefa = coletarDados()
            if (tarefa != null) {
                criarTarefa(requireContext(), tarefa)
            }
        }

        botaoCancelar.setOnClickListener { dismiss() }
    }

    private fun coletarDados(): Task? = with(binding) {
        val titulo = inputTitulo.text.toString().trim()
        val descricao = inputDesc.text.toString().trim().ifBlank { null }
        val notificar = inputNotificar.isChecked
        val recorrencia = obterRecorrencia()
        val isRecorrente = recorrencia.any { it == '1' }

        if (isRecorrente) {
            if (horaSelecionada == null || minutoSelecionado == null) {
                Toast.makeText(requireContext(), "Selecione a hora", Toast.LENGTH_SHORT).show()
                return null
            }
        } else {
            if (diaSelecionadoLong == null) {
                Toast.makeText(requireContext(), "Selecione o dia", Toast.LENGTH_SHORT).show()
                return null
            }
            if (horaSelecionada == null || minutoSelecionado == null) {
                Toast.makeText(requireContext(), "Selecione a hora", Toast.LENGTH_SHORT).show()
                return null
            }
        }

        // Calcula millis para o horário (usando a data atual só como base para exibir/ordenar)
        val cal = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, horaSelecionada ?: 0)
            set(Calendar.MINUTE, minutoSelecionado ?: 0)
        }
        val horarioMillis = cal.timeInMillis

        val diaSelecionado: Long? = if (isRecorrente) null else diaSelecionadoLong

        return Task(
            id = UUID.randomUUID().toString(),
            userId = userId,
            title = titulo,
            description = descricao,
            day = diaSelecionado,
            daytime = horarioMillis,
            notify = notificar,
            recurring = recorrencia,
            updatedAt = System.currentTimeMillis(),
            completedAt = null
        )
    }

    private fun obterRecorrencia(): String = with(binding) {
        listOf(
            chipSeg, chipTer, chipQuar, chipQuin, chipSex, chipSab, chipDom
        ).joinToString("") { if (it.isChecked) "1" else "0" }
    }

    private fun criarTarefa(context: Context, task: Task) {
        val db = DatabaseRoom.getDatabase(context)
        val dao = db.taskLocalDao()

        lifecycleScope.launch {
            try {
                val tarefaLocal = TaskLocal(
                    id = task.id,
                    userId = task.userId,
                    title = task.title,
                    description = task.description,
                    day = task.day,
                    daytime = task.daytime,
                    notify = task.notify,
                    recurring = task.recurring,
                    updatedAt = task.updatedAt,
                    completedAt = task.completedAt,
                )

                dao.criarTarefa(tarefaLocal)
                // Agenda alarme simples se aplicável (não recorrente por enquanto)
                TaskAlarme.schedule(context, tarefaLocal)

                val call = RetrofitInstance.INSTANCE.criarTarefa(task)
                call.enqueue(object : Callback<Task> {
                    override fun onResponse(call: Call<Task>, response: Response<Task>) {
                        // Notifica fragment pai para atualizar lista
                        parentFragmentManager.setFragmentResult("novaTarefa", Bundle())
                        dismiss()
                    }

                    override fun onFailure(call: Call<Task>, t: Throwable) {
                        parentFragmentManager.setFragmentResult("novaTarefa", Bundle())
                        dismiss()
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Erro ao criar tarefa", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
