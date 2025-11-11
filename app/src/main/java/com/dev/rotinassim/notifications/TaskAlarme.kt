package com.dev.rotinassim.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dev.rotinassim.R
import com.dev.rotinassim.room.entities.TaskLocal
import android.util.Log
import com.dev.rotinassim.utils.PrefsUtils
import java.util.Date
import java.util.TimeZone
import com.dev.rotinassim.room.DatabaseRoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskAlarme : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) { // dispara quando o alarme toca
        val taskId = intent.getStringExtra("taskId") ?: return
        val titulo = intent.getStringExtra("titulo") ?: "Tarefa"
        val descricao = intent.getStringExtra("descricao") ?: ""
        Log.d("TaskAlarme", "onReceive disparado taskId=$taskId titulo=$titulo")

        val canalId = "canal_de_tarefas"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(NotificationManager::class.java)
            if (nm.getNotificationChannel(canalId) == null) {
                val canal = NotificationChannel(
                    canalId,
                    "Tarefas",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Notificações de tarefas agendadas" }
                nm.createNotificationChannel(canal)
                Log.d("TaskAlarme", "Canal de notificação criado id=$canalId")
            }
        }

        val notif = NotificationCompat.Builder(context, canalId)
            .setSmallIcon(R.mipmap.app_icon_round)
            .setContentTitle(titulo)
            .setContentText(descricao)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(taskId.hashCode(), notif)
            Log.d("TaskAlarme", "Notificação mostrada idHash=${taskId.hashCode()}")
        } catch (e: SecurityException) { Log.e("TaskAlarme", "Falha ao notificar: ${e.message}") }

        // Reagendar próxima ocorrência se recorrente
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = DatabaseRoom.getDatabase(context)
                val dao = db.taskLocalDao()
                val task = dao.buscarPorId(taskId) ?: return@launch
                if (!task.recurring.contains('1')) return@launch
                // Avança para o próximo dia ativo (a partir de amanhã)
                val pattern = task.recurring[6] + task.recurring.substring(0,6) // realinha para Date.day
                val agoraDate = Date()
                var candidato = Date(agoraDate.time + 86_400_000L)
                while (pattern[candidato.day] != '1') {
                    candidato.time += 86_400_000L
                }
                // Converte meia-noite local do candidato para 00:00Z para gravar em 'day'
                val localMidnight = Date(candidato.time).apply { hours = 0; minutes = 0; seconds = 0 }
                val tz = TimeZone.getDefault()
                val offsetMidnight = tz.getOffset(localMidnight.time)
                val dayUtc = localMidnight.time - offsetMidnight // utc = local - offset

                val proxima = task.copy(day = dayUtc)
                dao.upsert(proxima)
                Log.d("TaskAlarme", "Reagendando recorrente id=${task.id} novoDay=${dayUtc} (${Date(dayUtc)})")
                schedule(context, proxima)
            } catch (e: Exception) {
                Log.e("TaskAlarme", "Falha ao reagendar recorrente: ${e.message}")
            }
        }
    }

    companion object {
        fun schedule(context: Context, task: TaskLocal) {
            Log.d("TaskAlarme", "schedule() chamado id=${task.id} notify=${task.notify} recurring='${task.recurring}' day=${task.day} daytime=${task.daytime}")
            if (!task.notify || task.completedAt != null) { Log.d("TaskAlarme", "Abortando: notify=false"); return }
            val proxNoti = calcularProximaNotificacao(context, task)
            if (proxNoti == null) { Log.d("TaskAlarme", "Abortando: cálculo retornou null"); return }
            val agora = System.currentTimeMillis()
            Log.d("TaskAlarme", "Resultado cálculo proxNoti=$proxNoti (diff=${proxNoti - agora} ms)")
            if (proxNoti <= agora) { Log.d("TaskAlarme", "Abortando: horário já passou") ; return }

            val intent = Intent(context, TaskAlarme::class.java).apply {
                putExtra("taskId", task.id)
                putExtra("titulo", task.title)
                putExtra("descricao", task.description ?: "Lembrete")
            }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val requestCode = task.id.hashCode()
            val pi = PendingIntent.getBroadcast(context, requestCode, intent, flags)
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            try {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, proxNoti, pi)
                Log.i("TaskAlarme", "Alarme agendado para ${Date(proxNoti)} (${proxNoti})")
            } catch (e: SecurityException) {
                Log.e("TaskAlarme", "Falha ao agendar: ${e.message}")
            }
        }

        fun cancel(context: Context, taskId: String) {
            val intent = Intent(context, TaskAlarme::class.java)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val pi = PendingIntent.getBroadcast(context, taskId.hashCode(), intent, flags)
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.cancel(pi)
            Log.d("TaskAlarme", "Alarme cancelado requestCode=${taskId.hashCode()}")
        }

        private fun calcularProximaNotificacao(context: Context, task: TaskLocal): Long? {
            val rec = task.recurring
            val isRecorrente = !rec.isNullOrEmpty() && rec.contains('1')
            val antecedenciaMin = PrefsUtils.getNotiTime(context)
            val antecedenciaMs = antecedenciaMin * 60_000L
            val baseHora = Date(task.daytime)
            val tz = TimeZone.getDefault()
            Log.d("TaskAlarme", "Antecedencia=${antecedenciaMin}min (${antecedenciaMs}ms) isRecorrente=$isRecorrente")

            if (isRecorrente) {
                val pattern = task.recurring[6] + task.recurring.substring(0,6)
                val agora = System.currentTimeMillis()
                val hoje = Date()
                for (i in 0..7) {
                    val d = Date(hoje.time + i * 86_400_000L)
                    if (pattern[d.day] != '1') continue
                    d.hours = baseHora.hours
                    d.minutes = baseHora.minutes
                    d.seconds = 0
                    val alvo = d.time
                    val comAnt = alvo - antecedenciaMs
                    if (comAnt > agora) return comAnt
                }
                return null
            }

            val dayUtcMs = task.day ?: return null // não recorrente precisa de dia
            val h = baseHora.hours
            val m = baseHora.minutes
            val timeOfDayMs = h * 3_600_000L + m * 60_000L
            val guess = dayUtcMs + timeOfDayMs
            val offsetAtTarget = tz.getOffset(guess)
            val alvo = dayUtcMs - offsetAtTarget + timeOfDayMs // epoch UTC do horário local selecionado
            val comAntecedencia = alvo - antecedenciaMs
            val agora = System.currentTimeMillis()
            Log.d("TaskAlarme", "dayUtcMs=${dayUtcMs} h=${h} m=${m} offset=${offsetAtTarget} alvo=${alvo} comAntecedencia=${comAntecedencia} agora=${agora}")
            return when {
                comAntecedencia > agora -> comAntecedencia
                alvo > agora -> { Log.d("TaskAlarme", "Antecedência tornou passado, usando alvo direto"); alvo }
                else -> null
            }
        }
    }
}
