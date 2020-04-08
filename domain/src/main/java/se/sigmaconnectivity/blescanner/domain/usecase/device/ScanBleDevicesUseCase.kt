package se.sigmaconnectivity.blescanner.domain.usecase.device

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import se.sigmaconnectivity.blescanner.domain.ble.BleScanner
import se.sigmaconnectivity.blescanner.domain.executor.PostExecutionThread
import se.sigmaconnectivity.blescanner.domain.model.ScanResultItem

class ScanBleDevicesUseCase(
    private val postExecutionThread: PostExecutionThread,
    private val bleScanner: BleScanner
) {

    fun execute(serviceUuids: List<String>, timeout: Long): Observable<ScanResultItem> =
        bleScanner.scanBleDevicesWithTimeout(serviceUuids, timeout)
            .subscribeOn(Schedulers.io())
            .observeOn(postExecutionThread.scheduler)
}