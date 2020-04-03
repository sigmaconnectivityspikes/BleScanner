package se.sigmaconnectivity.blescanner.domain.usecase

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import se.sigmaconnectivity.blescanner.domain.executor.PostExecutionThread

class TrackHasUserHadContactWithInfectedUseCase(
    private val postExecutionThread: PostExecutionThread,
    private val trackInfectionsUseCase: TrackInfectionsUseCase,
    private val hasUserHadContactWithInfectedUseCase: HasUserHadContactWithInfectedUseCase
    ) {
    fun execute(): Observable<Boolean> = trackInfectionsUseCase.execute()
        .flatMapSingle {
            hasUserHadContactWithInfectedUseCase.execute(it.hashId)}
        .subscribeOn(Schedulers.io())
        .observeOn(postExecutionThread.scheduler)
}