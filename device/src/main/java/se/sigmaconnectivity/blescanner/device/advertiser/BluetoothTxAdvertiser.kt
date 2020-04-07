package se.sigmaconnectivity.blescanner.device.advertiser

import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import io.reactivex.Observable
import se.sigmaconnectivity.blescanner.device.BLEFeatureState
import java.util.*

class BluetoothTxAdvertiser(private val context: Context) : BluetoothAdvertiser() {
    override val bluetoothLeAdvertiser: BluetoothLeAdvertiser? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter.bluetoothLeAdvertiser
    }

    private val settings: AdvertiseSettings = AdvertiseSettings.Builder()
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
        .setConnectable(false)
        .setTimeout(0)
        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW)
        .build()

    fun startAdvertising(serviceUUID: UUID): Observable<BLEFeatureState> {
        val data = buildData(serviceUUID)
        return super.startAdvertising(settings, data)
    }

    private fun buildData(
        serviceUUID: UUID
    ): AdvertiseData {
        return AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(true)
            .addServiceUuid(ParcelUuid(serviceUUID))
            .build()
    }
}