package com.example.appm_trilheiros.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.appm_trilheiros.R
import com.example.appm_trilheiros.views.MainActivity

class MyForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Criar o canal de notificação
        NotificationHelper.createNotificationChannel(this)

        // Exibe a notificação enquanto o serviço está rodando
        val notification = createNotification()
        startForeground(1, notification)

        // Lógica do serviço (exemplo de sincronização de dados)
        // Coloque aqui a lógica do que o serviço vai fazer enquanto rodando
        // Exemplo de chamada de sincronização de dados:
        // syncData()

        return START_STICKY
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
            .setContentTitle("App rodando em segundo plano")
            .setContentText("Este aplicativo está em execução para sincronização de dados")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)  // Impede que a notificação seja descartada pelo usuário
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Parar qualquer lógica adicional quando o serviço for destruído
    }
}
