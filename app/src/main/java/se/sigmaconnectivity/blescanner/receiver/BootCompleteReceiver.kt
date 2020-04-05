package se.sigmaconnectivity.blescanner.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat
import se.sigmaconnectivity.blescanner.R
import se.sigmaconnectivity.blescanner.extensions.isBluetoothEnabled
import se.sigmaconnectivity.blescanner.service.BleScanService


class BootCompleteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent) {
        context?.let {
            if (it.isBluetoothEnabled()) {
                ContextCompat.startForegroundService(
                    it,
                    Intent(context, BleScanService::class.java)
                )
            } else {
                Toast.makeText(
                    it,
                    "Please enable Bluetooth to use ${context.getString(R.string.app_name)}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}