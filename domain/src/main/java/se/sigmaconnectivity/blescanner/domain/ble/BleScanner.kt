package se.sigmaconnectivity.blescanner.domain.ble

import io.reactivex.Observable
import se.sigmaconnectivity.blescanner.domain.model.BLEFeatureState
import se.sigmaconnectivity.blescanner.domain.model.ScanResultItem

interface BleScanner {
    fun scanBleDevicesWithTimeout(
        serviceUuid: String,
        timeoutMillis: Long
    ): Observable<ScanResultItem>

    val trackScanningStatus: Observable<BLEFeatureState>
}