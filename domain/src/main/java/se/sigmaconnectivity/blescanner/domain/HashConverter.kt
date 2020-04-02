package se.sigmaconnectivity.blescanner.domain

import io.reactivex.Single

interface HashConverter {
    fun convert(input: ByteArray): Single<String>
}