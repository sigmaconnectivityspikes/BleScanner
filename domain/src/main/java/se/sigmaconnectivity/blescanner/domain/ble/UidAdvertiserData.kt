package se.sigmaconnectivity.blescanner.domain.ble

class UidAdvertiserData(
    override val serviceUUID: String,
    val manufacturerId: Int,
    val userUid: String
): AdvertiserData