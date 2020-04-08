package se.sigmaconnectivity.blescanner.domain.ble

import io.reactivex.Observable
import se.sigmaconnectivity.blescanner.domain.model.BLEFeatureState
import se.sigmaconnectivity.blescanner.domain.model.ScanResultItem

interface BleScanner {

    val trackScanningStatus: Observable<BLEFeatureState>
    fun scanBleDevicesWithTimeout(
        serviceUuids: List<String>,
        timeoutMillis: Long
    ): Observable<ScanResultItem>
}