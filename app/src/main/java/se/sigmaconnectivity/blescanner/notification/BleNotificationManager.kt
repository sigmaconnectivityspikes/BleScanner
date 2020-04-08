package se.sigmaconnectivity.blescanner.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import se.sigmaconnectivity.blescanner.Consts
import se.sigmaconnectivity.blescanner.R
import se.sigmaconnectivity.blescanner.ui.MainActivity
import se.sigmaconnectivity.blescanner.ui.feature.FeatureStatus

class BleNotificationManager(private val context: Context) {

    private val notificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }

    private var scanStatus = FeatureStatus.INACTIVE
    private var advertiseStatus = FeatureStatus.INACTIVE

    fun updateNotification(
        scan: FeatureStatus = scanStatus,
        adv: FeatureStatus = advertiseStatus
    ) {
        scanStatus = scan
        advertiseStatus = adv
        notificationManager?.notify(Consts.NOTIFICATION_ID, createNotification())
    }

    fun createNotification(): Notification {
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            0
        )
        return NotificationCompat.Builder(context, Consts.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.app_name))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        context.getString(
                            R.string.notification_content,
                            context.getString(scanStatus.nameRes),
                            context.getString(advertiseStatus.nameRes)
                        )
                    )
            )
            .setContentIntent(pendingIntent)
            .build()
    }
}