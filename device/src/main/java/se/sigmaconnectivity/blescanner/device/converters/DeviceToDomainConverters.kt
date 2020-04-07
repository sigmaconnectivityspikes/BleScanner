package se.sigmaconnectivity.blescanner.device.converters

import android.bluetooth.le.ScanResult
import android.util.SparseArray
import androidx.core.util.forEach
import se.sigmaconnectivity.blescanner.domain.model.ScanResultItem

fun ScanResult.toDomainItem() =
    ScanResultItem(
        manufacturerSpecificData = scanRecord?.manufacturerSpecificData?.toMap() ?: emptyMap()
    )

fun<E> SparseArray<E>.toMap(): Map<Int, E> {
    val list = HashMap<Int, E>()
    forEach {key, value ->
        list[key] = value
    }
    return list
}