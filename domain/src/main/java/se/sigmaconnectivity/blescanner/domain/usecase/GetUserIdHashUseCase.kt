package se.sigmaconnectivity.blescanner.domain.usecase

import io.reactivex.schedulers.Schedulers
import se.sigmaconnectivity.blescanner.domain.UidRepository
import se.sigmaconnectivity.blescanner.domain.executor.PostExecutionThread

class GetUserIdHashUseCase(
    private val postExecutionThread: PostExecutionThread,
    private val repository: UidRepository
) {

    fun execute() = repository.getUidHash()
        .subscribeOn(Schedulers.io())
        .observeOn(postExecutionThread.scheduler)
}