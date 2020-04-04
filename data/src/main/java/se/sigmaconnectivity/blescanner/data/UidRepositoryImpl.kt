package se.sigmaconnectivity.blescanner.data

import io.reactivex.Single
import se.sigmaconnectivity.blescanner.data.db.SharedPrefs
import se.sigmaconnectivity.blescanner.domain.UidRepository

const val HASH_SIZE_BYTES = 7

class UidRepositoryImpl(
    private val sharedPrefs: SharedPrefs
) : UidRepository {
    override fun getUidHash(): Single<ByteArray> = Single.fromCallable {
        sharedPrefs.getUserUuid().toHash()
    }
}