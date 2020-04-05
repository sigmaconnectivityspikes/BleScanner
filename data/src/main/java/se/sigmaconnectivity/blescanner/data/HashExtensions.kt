package se.sigmaconnectivity.blescanner.data

import java.security.MessageDigest

fun ByteArray.toChecksum(): Byte {
    var sum = 0
    forEach {
        sum = sum shl 1
        sum += it
    }
    return sum.toByte()
}

fun ByteArray.isValidChecksum(checksum: Byte): Boolean = toChecksum() == checksum