package se.sigmaconnectivity.blescanner.domain.entity

import java.io.Serializable


sealed class Entity : Serializable {
    data class Contact(//TODO field names changes for gson reason
        val id: Long = 0,
        val name: String,
        var status: Int = STATUS_MATCHED,
        var timestamp: Long = 0,
        var lostTimestamp: Long = 0,
        var duration: Long = 0
    ): Entity()
}

const val STATUS_MATCHED = 1
const val STATUS_LOST = 2