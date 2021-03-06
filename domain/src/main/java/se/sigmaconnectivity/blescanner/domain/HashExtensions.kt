package se.sigmaconnectivity.blescanner.domain

import java.security.MessageDigest


const val HASH_SIZE_BYTES = 8

fun String.toHash(): ByteArray {
    val md = MessageDigest.getInstance("SHA-1")
    md.update(toByteArray(Charsets.UTF_8))
    return md.digest().sliceArray(0 until HASH_SIZE_BYTES)
}