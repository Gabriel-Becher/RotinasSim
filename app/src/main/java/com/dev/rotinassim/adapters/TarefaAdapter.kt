package com.dev.rotinassim.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dev.rotinassim.R
import com.dev.rotinassim.room.entities.TaskLocal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TarefaAdapter(
    private val tarefas: MutableList<TaskLocal>,
    private val onConcluir: (TaskLocal) -> Unit,
    private val onExcluir: (TaskLocal) -> Unit
) : RecyclerView.Adapter<TarefaAdapter.TarefaViewHolder>() {

    inner class TarefaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titulo: TextView = itemView.findViewById(R.id.tituloTarefa)
        private val descricao: TextView = itemView.findViewById(R.id.descricaoTarefa)
        private val data: TextView = itemView.findViewById(R.id.dataTarefa)
        private val recorrencia: TextView = itemView.findViewById(R.id.recorrenciaTarefa)
        private val imagem: ImageView = itemView.findViewById(R.id.imagemTarefa)
        private val botaoConcluir: Button = itemView.findViewById(R.id.botaoConcluir)
        private val botaoExcluir: Button = itemView.findViewById(R.id.botaoExcluir)

        private val diasAbreviados = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")

        fun bind(tarefa: TaskLocal) {
            titulo.text = tarefa.title
            descricao.text = tarefa.description ?: ""

            // Evita mudança de dia ao converter de UTC para timezone local: fixa timezone em UTC
            val utc = java.util.TimeZone.getTimeZone("UTC")
            val horaFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val diaFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = utc }

            val dia = tarefa.day?.let { diaFormat.format(Date(it)) } ?: ""
            val hora = horaFormat.format(Date(tarefa.daytime))
            data.text = "${dia} ${hora}"

            // Exibe abreviações dos dias de recorrência
            val recorrente = tarefa.recurring
            if (!recorrente.isNullOrBlank()) {
                val diasSelecionados = recorrente.mapIndexedNotNull { index, c -> if (c == '1') diasAbreviados.getOrNull(index) else null }
                if (diasSelecionados.isNotEmpty()) {
                    recorrencia.visibility = View.VISIBLE
                    val todosRecurring = recorrente.take(diasAbreviados.size).all { it == '1' }
                    recorrencia.text = if (todosRecurring) "Todos os dias" else diasSelecionados.joinToString(", ")
                    imagem.setImageResource(R.drawable.recorrente_icon)
                } else {
                    recorrencia.visibility = View.GONE
                    recorrencia.text = ""
                    imagem.setImageResource(R.drawable.normal_icon)
                }
            } else {
                recorrencia.visibility = View.GONE
                recorrencia.text = ""
                imagem.setImageResource(R.drawable.normal_icon)
            }
            // Deixo o item mais fraco se estiver concluido
            if (tarefa.completedAt != null) {
                itemView.alpha = 0.7f
            } else {
                itemView.alpha = 1f
            }

            botaoConcluir.setOnClickListener { onConcluir(tarefa) }
            botaoExcluir.setOnClickListener { onExcluir(tarefa) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TarefaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lista, parent, false)
        return TarefaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TarefaViewHolder, position: Int) {
        holder.bind(tarefas[position])
    }

    override fun getItemCount(): Int = tarefas.size

    fun atualizarLista(novaLista: List<TaskLocal>) {
        tarefas.clear()
        tarefas.addAll(novaLista)
        notifyDataSetChanged()
    }
}
