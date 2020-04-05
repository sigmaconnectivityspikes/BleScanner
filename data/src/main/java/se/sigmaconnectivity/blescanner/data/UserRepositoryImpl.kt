package se.sigmaconnectivity.blescanner.data

import io.reactivex.Completable
import io.reactivex.Single
import se.sigmaconnectivity.blescanner.data.db.SharedPrefs
import se.sigmaconnectivity.blescanner.domain.UserRepository

class UserRepositoryImpl(
    private val sharedPrefs: SharedPrefs
) : UserRepository {

    override fun getUserHash(): Single<String> = Single.fromCallable {
        sharedPrefs.getUserHash()
    }

    override fun saveUserHash(hash: String) = Completable.fromCallable {
        sharedPrefs.setUserHash(hash)
    }
}