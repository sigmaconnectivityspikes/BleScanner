package se.sigmaconnectivity.blescanner.data

import io.reactivex.Single
import se.sigmaconnectivity.blescanner.domain.HashConverter

class HexHashConverter : HashConverter {
    override fun convert(input: ByteArray): Single<String> = Single.fromCallable {
        input.toHexString()
    }

    private fun ByteArray.toHexString() : String {
        return this.joinToString("") {
            java.lang.String.format("%02x", it)
        }
    }
}