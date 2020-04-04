package se.sigmaconnectivity.blescanner.data

fun ByteArray.toChecksum() = sum().toByte()

fun ByteArray.isValidChecksum(checksum: Byte): Boolean = toChecksum() == checksum