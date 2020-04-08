package se.sigmaconnectivity.blescanner.data

import se.sigmaconnectivity.blescanner.domain.HashConverter

class HexHashConverter : HashConverter {
    override fun convert(input: ByteArray): String = input.toHexString()

    private fun ByteArray.toHexString() : String {
        return this.joinToString("") {
            java.lang.String.format("%02x", it)
        }
    }
}