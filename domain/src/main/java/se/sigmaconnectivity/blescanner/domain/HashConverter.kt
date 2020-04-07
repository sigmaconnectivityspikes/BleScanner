package se.sigmaconnectivity.blescanner.domain

interface HashConverter {
    fun convert(input: ByteArray): String
}