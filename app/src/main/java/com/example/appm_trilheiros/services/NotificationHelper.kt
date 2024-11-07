package com.example.appm_trilheiros.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.example.appm_trilheiros.R

object NotificationHelper {

    const val CHANNEL_ID = "foreground_service_channel"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "App Background Service"
            val descriptionText = "Canal de serviço em segundo plano"
            val importance = NotificationManager.IMPORTANCE_DEFAULT  // Use IMPORTANCE_HIGH se for uma notificação mais importante

            // Cria o canal de notificação
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Verifica se o canal já foi criado para evitar recriação desnecessária
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    fun getNotificationManager(context: Context): NotificationManagerCompat {
        return NotificationManagerCompat.from(context)
    }
}
