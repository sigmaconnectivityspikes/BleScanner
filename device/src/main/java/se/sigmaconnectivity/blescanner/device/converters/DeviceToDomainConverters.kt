package se.sigmaconnectivity.blescanner.device.converters

import android.bluetooth.le.ScanResult
import android.util.SparseArray
import androidx.core.util.forEach
import se.sigmaconnectivity.blescanner.domain.model.ScanResultItem

fun ScanResult.toDomainItem(timestamp: Long) =
    with(this) {
        ScanResultItem(
            manufacturerSpecificData = scanRecord?.manufacturerSpecificData?.toMap() ?: emptyMap(),
            serviceUuid = scanRecord?.serviceUuids?.firstOrNull()?.toString()
                ?: throw IllegalStateException("Service Uuid has no value"),
            txPowerLevel = scanRecord?.txPowerLevel,
            timeStamp = timestamp,
            address = device.address,
            rssi = rssi
        )
    }

fun <E> SparseArray<E>.toMap(): Map<Int, E> {
    val list = HashMap<Int, E>()
    forEach { key, value ->
        list[key] = value
    }
    return list
}