package com.callcenter.smartclass.ui.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.callcenter.smartclass.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Jadwal Menu Anak"
        val message = intent.getStringExtra("message") ?: "Waktunya memberi makan anak!"
        val notificationId = intent.getIntExtra("notificationId", 0)

        val builder = NotificationCompat.Builder(context, "jadwal_menu_channel")
            .setSmallIcon(R.drawable.assets_logo_smartclass_clean)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
}