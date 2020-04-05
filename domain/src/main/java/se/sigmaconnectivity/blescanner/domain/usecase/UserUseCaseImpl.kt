package se.sigmaconnectivity.blescanner.domain.usecase

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import se.sigmaconnectivity.blescanner.domain.UserRepository
import se.sigmaconnectivity.blescanner.domain.executor.PostExecutionThread

class UserUseCaseImpl(
    private val postExecutionThread: PostExecutionThread,
    private val userRepository: UserRepository
) : UserUseCase {
    override fun saveUserHash(hash: String): Completable = userRepository.saveUserHash(hash)
        .subscribeOn(Schedulers.io())
        .observeOn(postExecutionThread.scheduler)

    override fun getUserHash(): Single<String> = userRepository.getUserHash()
        .subscribeOn(Schedulers.io())
        .observeOn(postExecutionThread.scheduler)

}