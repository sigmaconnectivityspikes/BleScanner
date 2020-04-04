package se.sigmaconnectivity.blescanner.data

import java.security.MessageDigest

fun String.toHash(): ByteArray {
    val md = MessageDigest.getInstance("SHA-1")
    md.update(toByteArray(Charsets.UTF_8))
    return md.digest().sliceArray(0 until HASH_SIZE_BYTES)
}

fun ByteArray.toChecksum() = sum().toByte()

fun ByteArray.isValidChecksum(checksum: Byte): Boolean = toChecksum() == checksum