package se.sigmaconnectivity.blescanner.domain.usecase.device

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import se.sigmaconnectivity.blescanner.domain.ble.BleUidAdvertiser
import se.sigmaconnectivity.blescanner.domain.executor.PostExecutionThread
import se.sigmaconnectivity.blescanner.domain.model.BLEFeatureState
import se.sigmaconnectivity.blescanner.domain.usecase.GetUserIdHashUseCase

class AdvertiseUidUseCase(
    private val postExecutionThread: PostExecutionThread,
    private val bleUidAdvertiser: BleUidAdvertiser,
    private val getUserIdHashUseCase: GetUserIdHashUseCase
) {

    fun execute(
        serviceUUID: String,
        manufacturerId: Int
    ): Observable<BLEFeatureState> {
        return getUserIdHashUseCase.execute()
            .flatMapObservable { userUid ->
                bleUidAdvertiser.startAdvertising(
                    serviceUUID,
                    manufacturerId,
                    userUid
                )
            }
            .subscribeOn(Schedulers.io())
            .observeOn(postExecutionThread.scheduler)
    }

}