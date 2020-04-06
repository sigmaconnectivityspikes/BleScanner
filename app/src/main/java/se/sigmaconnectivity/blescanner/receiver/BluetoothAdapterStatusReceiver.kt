package se.sigmaconnectivity.blescanner.receiver

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat
import org.koin.core.KoinComponent
import org.koin.core.inject
import se.sigmaconnectivity.blescanner.R
import se.sigmaconnectivity.blescanner.domain.BluetoothStatusRepository
import se.sigmaconnectivity.blescanner.domain.model.BluetoothStatus
import se.sigmaconnectivity.blescanner.service.BleScanService
import timber.log.Timber

class BluetoothAdapterStatusReceiver : BroadcastReceiver(), KoinComponent {

    private val repository: BluetoothStatusRepository by inject()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && BluetoothAdapter.ACTION_STATE_CHANGED == intent.action) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
            Timber.d("Received BluetoothAdapter state changed, state = [$state]")
            when (state) {
                BluetoothAdapter.STATE_OFF -> {
                    context?.let {
                        repository.putBluetoothStatus(BluetoothStatus.TurnedOff)
                        it.stopService(Intent(it, BleScanService::class.java))
                        Toast.makeText(
                            it,
                            "Please enable Bluetooth to use ${context.getString(R.string.app_name)}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                BluetoothAdapter.STATE_ON -> {
                    context?.let {
                        repository.putBluetoothStatus(BluetoothStatus.TurnedOn)
                        Timber.d("Bluetooth enabled, starting BleScanService")
                        ContextCompat.startForegroundService(
                            it,
                            Intent(it, BleScanService::class.java)
                        )
                    }
                }
            }
        }
    }
}