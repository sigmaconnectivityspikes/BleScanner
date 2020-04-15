package se.sigmaconnectivity.blescanner.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.reactivex.disposables.CompositeDisposable
import org.koin.android.ext.android.inject
import se.sigmaconnectivity.blescanner.data.mapper.hasNotification
import se.sigmaconnectivity.blescanner.data.mapper.toNotificationDataItem
import se.sigmaconnectivity.blescanner.ui.common.PushNotificationManager
import timber.log.Timber

class FcmService : FirebaseMessagingService() {
    private val compositeDisposable = CompositeDisposable()

    private val notificationManager: PushNotificationManager by inject()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // TODO(developer): Handle FCM messages here.
        Timber.d("FCM from: ${remoteMessage.from}")

        // Check if message contains a data payload.
        remoteMessage.data.isNotEmpty().let {
            handleNotification(remoteMessage)
        }
    }

    private fun handleNotification(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.hasNotification()) {
            remoteMessage.data.toNotificationDataItem().let {
                notificationManager.showNotificationWithData(
                    it.title,
                    it.content,
                    remoteMessage.data.toString()
                )
            }
        }
    }

    override fun onNewToken(token: String) {
        Timber.d("Refreshed token: $token")
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}