package se.sigmaconnectivity.blescanner.domain.usecase.device

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import se.sigmaconnectivity.blescanner.domain.ble.BleAdvertiser
import se.sigmaconnectivity.blescanner.domain.ble.TxAdvertiserData
import se.sigmaconnectivity.blescanner.domain.executor.PostExecutionThread
import se.sigmaconnectivity.blescanner.domain.model.BLEFeatureState

class AdvertiseTxUseCase(
    private val postExecutionThread: PostExecutionThread,
    private val bleAdvertiser: BleAdvertiser
) {

    fun execute(serviceUUID: String): Observable<BLEFeatureState> {
        val data = TxAdvertiserData(serviceUUID)
        return bleAdvertiser.startAdvertising(data)
            .doOnDispose { bleAdvertiser.stopAdvertising() }
            .subscribeOn(Schedulers.io())
            .observeOn(postExecutionThread.scheduler)
    }
}