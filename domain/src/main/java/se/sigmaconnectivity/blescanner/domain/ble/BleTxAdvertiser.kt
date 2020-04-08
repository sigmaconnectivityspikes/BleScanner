package se.sigmaconnectivity.blescanner.domain.ble

import io.reactivex.Observable
import se.sigmaconnectivity.blescanner.domain.model.BLEFeatureState

interface BleTxAdvertiser {
    fun startAdvertising(serviceUUID: String): Observable<BLEFeatureState>
}