package se.sigmaconnectivity.blescanner.data

import java.security.MessageDigest


const val HASH_SIZE_BYTES = 7

fun String.toHash(): ByteArray {
    val md = MessageDigest.getInstance("SHA-1")
    md.update(toByteArray(Charsets.UTF_8))
    return md.digest().sliceArray(0 until HASH_SIZE_BYTES)
}

fun ByteArray.toChecksum(): Byte {
    var sum = 0
    forEach {
        sum = sum shl 1
        sum += it
    }
    return sum.toByte()
}

fun ByteArray.isValidChecksum(checksum: Byte): Boolean = toChecksum() == checksum