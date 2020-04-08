package se.sigmaconnectivity.blescanner.domain.usecase.device

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import se.sigmaconnectivity.blescanner.domain.ble.BleAdvertiser
import se.sigmaconnectivity.blescanner.domain.ble.UidAdvertiserData
import se.sigmaconnectivity.blescanner.domain.executor.PostExecutionThread
import se.sigmaconnectivity.blescanner.domain.model.BLEFeatureState

class AdvertiseUidUseCase (
    private val postExecutionThread: PostExecutionThread,
    private val bleAdvertiser: BleAdvertiser
) {

    fun execute(uidAdvertiserData: UidAdvertiserData): Observable<BLEFeatureState> =
        bleAdvertiser.startAdvertising(uidAdvertiserData)
            .subscribeOn(Schedulers.io())
            .observeOn(postExecutionThread.scheduler)
}