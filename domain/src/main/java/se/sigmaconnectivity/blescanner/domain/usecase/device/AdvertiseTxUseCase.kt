package se.sigmaconnectivity.blescanner.domain.usecase.device

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import se.sigmaconnectivity.blescanner.domain.ble.BleTxAdvertiser
import se.sigmaconnectivity.blescanner.domain.executor.PostExecutionThread
import se.sigmaconnectivity.blescanner.domain.model.BLEFeatureState

class AdvertiseTxUseCase(
    private val postExecutionThread: PostExecutionThread,
    private val bleUidAdvertiser: BleTxAdvertiser
) {

    fun execute(serviceUUID: String): Observable<BLEFeatureState> =
        bleUidAdvertiser.startAdvertising(serviceUUID)
            .subscribeOn(Schedulers.io())
            .observeOn(postExecutionThread.scheduler)
}