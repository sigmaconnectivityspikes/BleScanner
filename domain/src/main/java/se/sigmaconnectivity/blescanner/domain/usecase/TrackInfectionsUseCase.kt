package se.sigmaconnectivity.blescanner.domain.usecase

import io.reactivex.schedulers.Schedulers
import se.sigmaconnectivity.blescanner.domain.PushNotifier
import se.sigmaconnectivity.blescanner.domain.executor.PostExecutionThread

class TrackInfectionsUseCase(
    private val postExecutionThread: PostExecutionThread,
    private val pushNotifier: PushNotifier
) {

    fun execute() = pushNotifier.trackInfectionNotifications
        .subscribeOn(Schedulers.io())
        .observeOn(postExecutionThread.scheduler)
}