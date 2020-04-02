package se.sigmaconnectivity.blescanner.data

import android.util.Base64
import io.reactivex.Single
import se.sigmaconnectivity.blescanner.domain.HashConverter

class Base64HashConverter : HashConverter {
    override fun convert(input: ByteArray): Single<String> = Single.fromCallable {
        Base64.encodeToString(input, Base64.DEFAULT)
    }
}