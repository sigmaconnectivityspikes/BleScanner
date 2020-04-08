package se.sigmaconnectivity.blescanner.domain.ble

import io.reactivex.Observable
import se.sigmaconnectivity.blescanner.domain.model.BLEFeatureState

interface BleUidAdvertiser {
    fun startAdvertising(serviceUUID: String, manufacturerId: Int, userUid: String): Observable<BLEFeatureState>
}