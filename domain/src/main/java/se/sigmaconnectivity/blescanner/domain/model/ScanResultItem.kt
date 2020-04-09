package se.sigmaconnectivity.blescanner.domain.model

data class ScanResultItem (
    val manufacturerSpecificData: Map<Int, ByteArray>,
    val serviceUuid: String,
    val timestamp: Long,
    val rssi: Int,
    val txPowerLevel: Int?
)
