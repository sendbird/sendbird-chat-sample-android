package com.sendbird.chat.sample.groupchannel.push.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sendbird.chat.sample.groupchannel.push.R


private const val ChannelId = "channel_id"
private const val NotificationId = 1

fun sendNotification(
    context: Context,
    messageTitle: String?,
    messageBody: String
) {

    val notification = NotificationCompat.Builder(context, ChannelId)
        .setSmallIcon(R.drawable.ic_logo_symbol)
        .setColor(Color.parseColor("#7469C4")) // small icon background color
        .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_logo_horizontal))
        .setContentTitle(messageTitle)
        .setContentText(messageBody)
        .setAutoCancel(true)
        .build()

    context.createNotificationChannel()

    with(NotificationManagerCompat.from(context)) {
        notify(NotificationId, notification)
    }
}


private fun Context.createNotificationChannel() {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(ChannelId, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
