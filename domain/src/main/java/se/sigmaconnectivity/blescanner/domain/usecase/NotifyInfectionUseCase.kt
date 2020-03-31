package se.sigmaconnectivity.blescanner.domain.usecase

import io.reactivex.schedulers.Schedulers
import se.sigmaconnectivity.blescanner.domain.PushNotifier
import se.sigmaconnectivity.blescanner.domain.executor.PostExecutionThread
import se.sigmaconnectivity.blescanner.domain.model.InfectionItem

class NotifyInfectionUseCase(
    private val postExecutionThread: PostExecutionThread,
    private val pushNotifier: PushNotifier
) {

    fun execute(infectionItem: InfectionItem) = pushNotifier.notifyInfection(infectionItem)
        .subscribeOn(Schedulers.io())
        .observeOn(postExecutionThread.scheduler)
}