package se.sigmaconnectivity.blescanner.domain.model

data class ScanResultItem (
    val manufacturerSpecificData: Map<Int, ByteArray>,
    val txPowerLevel: Int?,
    val serviceUuid: String,
    val address: String,
    val timeStamp: Long,
    val rssi: Int
)
