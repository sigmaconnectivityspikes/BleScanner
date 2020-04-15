package se.sigmaconnectivity.blescanner.ui.common

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import se.sigmaconnectivity.blescanner.Consts
import se.sigmaconnectivity.blescanner.R
import se.sigmaconnectivity.blescanner.ui.MainActivity
import timber.log.Timber
import kotlin.random.Random

class PushNotificationManager(private val context: Context) {

    private val notificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }

    fun showNotification(title: String, content: String) {
        showNotificationWithID(Random.nextInt(), title, content)
    }

    fun showNotificationWithData(title: String, content: String, data: String) {
        showNotificationWithID(Random.nextInt(), title, content, data)
    }

    fun showNotificationWithID(
        notificationId: Int,
        title: String,
        content: String,
        data: String? = null
    ) {
        val notification = createNotification(title, content, data)
        notificationManager?.let {
            it.notify(notificationId, notification)
            Timber.d("Show notification: $title, $content")
        } ?: Timber.d("Show notification failed")
    }

    private fun createNotification(title: String, content: String, data: String?): Notification {
        val notificationIntent = Intent(context, MainActivity::class.java)
        data?.let {
            notificationIntent.putExtra(Consts.NOTIFICATION_EXTRA_DATA, it)
            notificationIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(context, Consts.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentText(content)
            .setContentIntent(pendingIntent)
            .build()
    }
}