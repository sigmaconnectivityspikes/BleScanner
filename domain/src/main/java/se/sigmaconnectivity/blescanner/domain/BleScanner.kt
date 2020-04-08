package se.sigmaconnectivity.blescanner.domain

import io.reactivex.Observable
import se.sigmaconnectivity.blescanner.domain.model.BLEScanState
import se.sigmaconnectivity.blescanner.domain.model.ScanResultItem

interface BleScanner {
    fun scanBleDevicesWithTimeout(
        serviceUuid: String,
        timeoutMillis: Long
    ): Observable<ScanResultItem>

    val trackScanningStatus: Observable<BLEScanState>
}