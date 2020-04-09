package se.sigmaconnectivity.blescanner.device.advertiser

import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import se.sigmaconnectivity.blescanner.domain.ble.AdvertiserData
import se.sigmaconnectivity.blescanner.domain.ble.TxAdvertiserData
import se.sigmaconnectivity.blescanner.domain.toHashPrefix

class BleTxAdvertiser(private val context: Context) : BleAdvertiserImpl() {
    override val bluetoothLeAdvertiser: BluetoothLeAdvertiser? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter.bluetoothLeAdvertiser
    }
    override val settings: AdvertiseSettings = AdvertiseSettings.Builder()
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
        .setConnectable(false)
        .setTimeout(0)
        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW)
        .build()

    override fun buildData(data: AdvertiserData): AdvertiseData {
        data as TxAdvertiserData
        return AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(true)
            .addManufacturerData(data.manufacturerId, data.userUid.toHashPrefix())
            .addServiceUuid(ParcelUuid.fromString(data.serviceUUID))
            .build()
    }
}