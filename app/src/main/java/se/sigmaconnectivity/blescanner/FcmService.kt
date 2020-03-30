package se.sigmaconnectivity.blescanner

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

class FcmService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // TODO(developer): Handle FCM messages here.
        Timber.d("FCM from: ${remoteMessage.from}")

        // Check if message contains a data payload.
        remoteMessage.data.isNotEmpty().let {
            with(remoteMessage) {
                Timber.d("FCM message notification: ${notification?.title} ${notification?.body} $data")
                showToast("FCM message notification:  ${notification?.title} $data")
            }
        }
    }

    //TODO: just for debug purposes, we should communicate with UI better way
    private fun showToast(text: String) {
        val handler = Handler(Looper.getMainLooper())
        handler.post { Toast.makeText(baseContext, text, Toast.LENGTH_SHORT).show() }
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
}