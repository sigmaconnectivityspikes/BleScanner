package se.sigmaconnectivity.blescanner.domain.model

data class ContactItem(
    val hashId: String,
    val timestamp: Long,
    val distance: Double
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContactItem

        if (hashId != other.hashId) return false

        return true
    }

    override fun hashCode(): Int = hashId.hashCode()
}