package se.sigmaconnectivity.blescanner.domain.entity

import java.io.Serializable


sealed class Entity : Serializable {
    data class Contact(
        val hash: String,
        var lastTimeStamp: Long = 0,
        var contactCounter: Long = 0,
        var totalContactTime: Long = 0
    ): Entity()
}