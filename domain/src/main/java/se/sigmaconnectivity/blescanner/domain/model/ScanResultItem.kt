package se.sigmaconnectivity.blescanner.domain.model

data class ScanResultItem (
    val manufacturerSpecificData: Map<Int, ByteArray>
)