package se.sigmaconnectivity.blescanner.extensions

import android.bluetooth.BluetoothManager
import android.content.Context
import android.widget.Toast

fun Context.isBluetoothEnabled(): Boolean {
    return (getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)
        ?.adapter
        ?.isEnabled == true
}

fun Context.showToast(text: String) {
    Toast.makeText(
        this,
        text,
        Toast.LENGTH_LONG
    ).show()
}