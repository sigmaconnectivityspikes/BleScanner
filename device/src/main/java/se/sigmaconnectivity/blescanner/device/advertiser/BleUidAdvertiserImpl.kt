package se.sigmaconnectivity.blescanner.device.advertiser

import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import io.reactivex.Observable
import se.sigmaconnectivity.blescanner.domain.ble.BleUidAdvertiser
import se.sigmaconnectivity.blescanner.domain.model.BLEFeatureState
import se.sigmaconnectivity.blescanner.domain.toHash
import java.util.*

class BleUidAdvertiserImpl(private val context: Context) : BleAdvertiser(), BleUidAdvertiser {

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


    override fun startAdvertising(serviceUUID: String, manufacturerId: Int, userUid: String): Observable<BLEFeatureState> {
        val data: AdvertiseData = buildData(UUID.fromString(serviceUUID), manufacturerId, userUid.toHash())
        return super.startAdvertising(settings, data)
    }

    private fun buildData(
        serviceUUID: UUID,
        manufacturerId: Int,
        userUid: ByteArray
    ): AdvertiseData {
        return AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .addManufacturerData(manufacturerId, userUid)
            .addServiceUuid(ParcelUuid(serviceUUID))
            .build()
    }
}