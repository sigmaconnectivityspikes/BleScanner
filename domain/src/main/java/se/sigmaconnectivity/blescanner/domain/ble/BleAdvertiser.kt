package se.sigmaconnectivity.blescanner.domain.ble

import io.reactivex.Observable
import se.sigmaconnectivity.blescanner.domain.model.BLEFeatureState

interface BleAdvertiser {
    fun startAdvertising(advertiserData: AdvertiserData): Observable<BLEFeatureState>
    fun stopAdvertising()
}