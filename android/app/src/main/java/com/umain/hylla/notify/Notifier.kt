package com.umain.hylla.notify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.umain.hylla.MainActivity
import com.umain.hylla.R
import com.umain.hylla.fleet.DeepLink
import com.umain.hylla.fleet.Device

/**
 * Posts Hylla's two notifications. Tapping one opens its device through the chapter 13 link,
 * so a notification needs no navigation code of its own.
 */
class Notifier(private val context: Context, private val enabled: () -> Boolean) {

    fun createChannels() {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannels(
            listOf(
                NotificationChannel(OVERDUE, context.getString(R.string.channel_overdue), NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(BACK, context.getString(R.string.channel_back), NotificationManager.IMPORTANCE_HIGH),
            ),
        )
    }

    fun overdue(device: Device) = post(
        id = device.id.value.hashCode(),
        builder = base(OVERDUE, device)
            .setContentTitle(context.getString(R.string.notify_overdue_title, device.deviceName))
            .setContentText(context.getString(R.string.notify_overdue_text))
            .addAction(0, context.getString(R.string.scan_return), returnIntent(device)),
    )

    fun backOnTheShelf(device: Device) = post(
        id = device.id.value.hashCode() + 1,
        builder = base(BACK, device)
            .setContentTitle(context.getString(R.string.notify_back_title, device.deviceName))
            .setContentText(context.getString(R.string.notify_back_text)),
    )

    fun cancelOverdue(device: Device) = NotificationManagerCompat.from(context).cancel(device.id.value.hashCode())

    private fun base(channel: String, device: Device) = NotificationCompat.Builder(context, channel)
        .setSmallIcon(R.drawable.ic_launcher_monochrome)
        .setAutoCancel(true)
        .setContentIntent(
            PendingIntent.getActivity(
                context,
                device.id.value.hashCode(),
                Intent(Intent.ACTION_VIEW, DeepLink.device(device.id).toUri(), context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            ),
        )

    private fun returnIntent(device: Device): PendingIntent = PendingIntent.getBroadcast(
        context,
        device.id.value.hashCode(),
        Intent(context, ReturnReceiver::class.java).putExtra(ReturnReceiver.EXTRA_DEVICE, device.id.value),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private fun post(id: Int, builder: NotificationCompat.Builder) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!enabled() || !granted) return
        NotificationManagerCompat.from(context).notify(id, builder.build())
    }

    private companion object {
        const val OVERDUE = "overdue"
        const val BACK = "back"
    }
}
