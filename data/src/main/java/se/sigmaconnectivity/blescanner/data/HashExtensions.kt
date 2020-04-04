package se.sigmaconnectivity.blescanner.data

import timber.log.Timber
import java.security.MessageDigest

fun String.toHash(): ByteArray {
    val md = MessageDigest.getInstance("SHA-1")
    md.update(toByteArray(Charsets.UTF_8))
    return md.digest().sliceArray(0 until HASH_SIZE_BYTES)
}

fun ByteArray.toChecksum(): Byte {
    val res = sum().toByte()
    Timber.d("WNASILOWSKILOG  size; $size sum ${res.toInt()}")
    return res
}

fun ByteArray.isValidChecksum(checksum: Byte): Boolean = toChecksum() == checksum