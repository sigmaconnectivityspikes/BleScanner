package se.sigmaconnectivity.blescanner.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import se.sigmaconnectivity.blescanner.service.BleScanService


class BootCompleteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent) {
        context?.let {
            ContextCompat.startForegroundService(
                it,
                Intent(context, BleScanService::class.java)
            )
        }
    }
}