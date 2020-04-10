package se.sigmaconnectivity.blescanner.domain.ble

class TxAdvertiserData(
    override val serviceUUID: String,
    val manufacturerId: Int,
    val userUid: String
): AdvertiserData