package se.sigmaconnectivity.blescanner.domain.usecase

import io.reactivex.Completable
import io.reactivex.Single

interface UserUseCase {
    fun saveUserHash(hash: String): Completable
    fun getUserHash(): Single<String>
}