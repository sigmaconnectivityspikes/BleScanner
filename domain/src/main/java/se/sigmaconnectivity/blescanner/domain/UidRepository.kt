package se.sigmaconnectivity.blescanner.domain

import io.reactivex.Single

interface UidRepository {
    fun getUidHash(): Single<ByteArray>
}