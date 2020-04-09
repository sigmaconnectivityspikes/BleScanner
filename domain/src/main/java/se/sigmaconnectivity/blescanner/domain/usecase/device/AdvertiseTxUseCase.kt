package se.sigmaconnectivity.blescanner.domain.usecase.device

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import se.sigmaconnectivity.blescanner.domain.ble.BleAdvertiser
import se.sigmaconnectivity.blescanner.domain.ble.TxAdvertiserData
import se.sigmaconnectivity.blescanner.domain.executor.PostExecutionThread
import se.sigmaconnectivity.blescanner.domain.model.BLEFeatureState
import se.sigmaconnectivity.blescanner.domain.usecase.GetUserIdHashUseCase

class AdvertiseTxUseCase(
    private val postExecutionThread: PostExecutionThread,
    private val bleAdvertiser: BleAdvertiser,
    private val getUserIdHashUseCase: GetUserIdHashUseCase
) {

    fun execute(serviceUUID: String, manufacturerId: Int): Observable<BLEFeatureState> =
        getUserIdHashUseCase.execute()
            .flatMapObservable { userUid ->
                val data = TxAdvertiserData(serviceUUID, manufacturerId, userUid)
                bleAdvertiser.startAdvertising(data)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(postExecutionThread.scheduler)
}