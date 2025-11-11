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
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.time.Duration.Companion.hours

class TaskAlarme : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) { // dispara quando o alarme toca
        val taskId = intent.getStringExtra("taskId") ?: return
        val titulo = intent.getStringExtra("titulo") ?: "Tarefa"
        val descricao = intent.getStringExtra("descricao") ?: ""
        Log.d("TaskAlarme", "onReceive disparado taskId=$taskId titulo=$titulo")

        val canalId = "canal_de_tarefas"

        // Preciso disso pra poder testar na api mais recente, mas funciona bem na api 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(NotificationManager::class.java)
            if (nm.getNotificationChannel(canalId) == null) {
                val canal = NotificationChannel(
                    canalId,
                    "Tarefas",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notificações de tarefas agendadas"
                }
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
    }

    companion object {
        fun schedule(context: Context, task: TaskLocal) {
            Log.d("TaskAlarme", "schedule() chamado id=${task.id} notify=${task.notify} recurring='${task.recurring}' day=${task.day} daytime=${task.daytime}")
            if (!task.notify) { Log.d("TaskAlarme", "Abortando: notify=false"); return }
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
            Log.d("TaskAlarme", "Intent criado extras taskId=${task.id}")
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val requestCode = task.id.hashCode()
            val pi = PendingIntent.getBroadcast(context, requestCode, intent, flags)
            Log.d("TaskAlarme", "PendingIntent criado requestCode=$requestCode flags=$flags")
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            try {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, proxNoti, pi)
                Log.i("TaskAlarme", "Alarme agendado com sucesso para ${Date(proxNoti)} (${proxNoti})")
            } catch (e: SecurityException) {
                Log.e("TaskAlarme", "Falha ao agendar: ${e.message}")
            }
        }

        fun cancel(context: Context, taskId: String) {
            Log.d("TaskAlarme", "cancel() chamado para id=$taskId")
            val intent = Intent(context, TaskAlarme::class.java)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val pi = PendingIntent.getBroadcast(context, taskId.hashCode(), intent, flags)
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.cancel(pi)
            Log.d("TaskAlarme", "Alarme cancelado requestCode=${taskId.hashCode()}")
        }

        // Calcula horário (millis) da próxima notificação para tarefa NÃO recorrente (tem 'day' e 'daytime')
        private fun calcularProximaNotificacao(context: Context, task: TaskLocal): Long? {
            val rec = task.recurring
            val isRecorrente = !rec.isNullOrEmpty() && rec.contains('1')
            val antecedenciaMin = PrefsUtils.getNotiTime(context)
            val antecedenciaMs = antecedenciaMin * 60_000L
            val baseDia = Date(task.day?:0)
            Log.i("BAE DIOA", "${baseDia.time}")
            val baseHora = Date(task.daytime)
            Log.d("TaskAlarme", "Verificando recorrência: recurring='${rec}' -> isRecorrente=$isRecorrente")
            if (isRecorrente) {

                val recorrenteLocal = task.recurring[6] + task.recurring.substring(0,6)
                val cal = Date()
                while (recorrenteLocal[cal.day] != '1' && cal.time < System.currentTimeMillis()-antecedenciaMs) {
                    cal.time += 24 * 60 * 60 * 1000
                }
                cal.hours = baseHora.hours
                cal.minutes = baseHora.minutes
                cal.seconds = 0

                return cal.time - antecedenciaMs
            }

            Log.d("TaskAlarme", "Antecedencia lida=${antecedenciaMin}min (${antecedenciaMs}ms)")

            if (baseDia == Date(0)) { Log.d("TaskAlarme", "Sem baseDia -> null") ; return null }
            val horario = baseDia
            horario.hours = baseHora.hours
            horario.minutes = baseHora.minutes
            horario.seconds = 0
            horario.time = horario.time - antecedenciaMs
            val agora = Date().time
            Log.i("Horario calculado", "${horario.time}")
            Log.i("Agora", "$agora")
            if(horario.time <= agora){
                return null
            }else{
                return horario.time
            }
        }
    }
}
