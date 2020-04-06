package se.sigmaconnectivity.blescanner.domain.usecase

import io.reactivex.schedulers.Schedulers
import se.sigmaconnectivity.blescanner.domain.UserRepository
import se.sigmaconnectivity.blescanner.domain.executor.PostExecutionThread

class GetUserIdHashUseCase(
    private val postExecutionThread: PostExecutionThread,
    private val repository: UserRepository
) {

    fun execute() = repository.getUserHash()
        .subscribeOn(Schedulers.io())
        .observeOn(postExecutionThread.scheduler)
}