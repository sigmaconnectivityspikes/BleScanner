package se.sigmaconnectivity.blescanner.domain.model

data class ScanResultItem(
    val hashId: String,
    val timestamp: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ScanResultItem

        if (hashId != other.hashId) return false

        return true
    }

    override fun hashCode(): Int = hashId.hashCode()
}