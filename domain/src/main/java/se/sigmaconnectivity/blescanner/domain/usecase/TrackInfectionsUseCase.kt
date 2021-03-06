package se.sigmaconnectivity.blescanner.domain.usecase

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import se.sigmaconnectivity.blescanner.domain.PushNotifier
import se.sigmaconnectivity.blescanner.domain.executor.PostExecutionThread
import se.sigmaconnectivity.blescanner.domain.model.InfectionItem

class TrackInfectionsUseCase(
    private val postExecutionThread: PostExecutionThread,
    private val pushNotifier: PushNotifier
) {

    fun execute(): Observable<InfectionItem> = pushNotifier.trackInfectionNotifications
        .subscribeOn(Schedulers.io())
        .observeOn(postExecutionThread.scheduler)
}