package se.sigmaconnectivity.blescanner.domain.usecase.device

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import se.sigmaconnectivity.blescanner.domain.LocationStatusRepository
import se.sigmaconnectivity.blescanner.domain.executor.PostExecutionThread
import se.sigmaconnectivity.blescanner.domain.model.LocationStatus

class SubscribeForLocationStatusUseCase(
    private val postExecutionThread: PostExecutionThread,
    private val repository: LocationStatusRepository
) {

    fun execute(): Observable<LocationStatus> =
        repository.trackLocationStatus
            .subscribeOn(Schedulers.io())
            .observeOn(postExecutionThread.scheduler)
}