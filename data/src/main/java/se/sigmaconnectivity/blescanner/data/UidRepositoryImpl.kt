package se.sigmaconnectivity.blescanner.data

import io.reactivex.Single
import se.sigmaconnectivity.blescanner.data.db.SharedPrefs
import se.sigmaconnectivity.blescanner.domain.UidRepository
import java.security.MessageDigest

const val HASH_SIZE_BYTES = 8

class UidRepositoryImpl(
    private val sharedPrefs: SharedPrefs
) : UidRepository {
    override fun getUidHash(): Single<ByteArray> = Single.fromCallable {
        sharedPrefs.getUserUuid().toHash()
        }

    private fun String.toHash(): ByteArray {
        val md = MessageDigest.getInstance("SHA-1")
        md.update(toByteArray(Charsets.UTF_8))
        return md.digest().sliceArray(0 until HASH_SIZE_BYTES)
    }
}