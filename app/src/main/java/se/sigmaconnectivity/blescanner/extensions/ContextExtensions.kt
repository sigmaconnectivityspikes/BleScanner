package se.sigmaconnectivity.blescanner.extensions

import android.bluetooth.BluetoothManager
import android.content.Context

fun Context.isBluetoothEnabled(): Boolean {
    return (getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)
        ?.adapter
        ?.isEnabled == true
}