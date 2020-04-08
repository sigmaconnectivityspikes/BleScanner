package se.sigmaconnectivity.blescanner.domain.usecase.device

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import se.sigmaconnectivity.blescanner.domain.ble.BleUidAdvertiser
import se.sigmaconnectivity.blescanner.domain.executor.PostExecutionThread
import se.sigmaconnectivity.blescanner.domain.model.BLEFeatureState

class AdvertiseUidUseCase(
    private val postExecutionThread: PostExecutionThread,
    private val bleUidAdvertiser: BleUidAdvertiser
) {

    fun execute(
        serviceUUID: String,
        manufacturerId: Int,
        userUid: String
    ): Observable<BLEFeatureState> =
        bleUidAdvertiser.startAdvertising(serviceUUID, manufacturerId, userUid)
            .subscribeOn(Schedulers.io())
            .observeOn(postExecutionThread.scheduler)
}