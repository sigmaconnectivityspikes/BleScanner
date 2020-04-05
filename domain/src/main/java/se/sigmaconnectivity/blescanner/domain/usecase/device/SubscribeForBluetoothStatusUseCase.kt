package se.sigmaconnectivity.blescanner.domain.usecase.device

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import se.sigmaconnectivity.blescanner.domain.BluetoothStatusRepository
import se.sigmaconnectivity.blescanner.domain.executor.PostExecutionThread
import se.sigmaconnectivity.blescanner.domain.model.BluetoothStatus

class SubscribeForBluetoothStatusUseCase(
    private val postExecutionThread: PostExecutionThread,
    private val repository: BluetoothStatusRepository
) {

    fun execute(): Observable<BluetoothStatus> =
        repository.getBluetoothStatus()
            .subscribeOn(Schedulers.io())
            .observeOn(postExecutionThread.scheduler)
}