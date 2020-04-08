package se.sigmaconnectivity.blescanner.device.advertiser

import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import io.reactivex.Observable
import se.sigmaconnectivity.blescanner.domain.ble.BleTxAdvertiser
import se.sigmaconnectivity.blescanner.domain.model.BLEFeatureState
import java.util.*

class BleTxAdvertiserImpl(private val context: Context) : BleAdvertiser(), BleTxAdvertiser {
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

    override fun startAdvertising(serviceUUID: String): Observable<BLEFeatureState> {
        val data = buildData(UUID.fromString(serviceUUID))
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