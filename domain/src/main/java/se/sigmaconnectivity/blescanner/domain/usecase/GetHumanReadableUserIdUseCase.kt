package se.sigmaconnectivity.blescanner.domain.usecase

import io.reactivex.schedulers.Schedulers
import se.sigmaconnectivity.blescanner.domain.HashConverter
import se.sigmaconnectivity.blescanner.domain.UidRepository
import se.sigmaconnectivity.blescanner.domain.executor.PostExecutionThread

class GetHumanReadableUserIdUseCase(
    private val postExecutionThread: PostExecutionThread,
    private val repository: UidRepository,
    private val hashConverter: HashConverter
) {

    fun execute() = repository.getUidHash()
        .flatMap { hashConverter.convert(it) }
        .subscribeOn(Schedulers.io())
        .observeOn(postExecutionThread.scheduler)
}