package se.sigmaconnectivity.blescanner.data.mapper

import se.sigmaconnectivity.blescanner.data.entity.Contact
import se.sigmaconnectivity.blescanner.domain.entity.Entity
import se.sigmaconnectivity.blescanner.domain.model.InfectionItem

fun Contact.dataToDomain() = Entity.Contact(
    id, hash, status, matchTimestamp, lostTimestamp, duration
)

private const val FCM_DATA_HASH_ID_KEY = "hash"
fun Map<String, String>.toInfectionItem() = InfectionItem(
    hashId = get(FCM_DATA_HASH_ID_KEY) ?: throw IllegalArgumentException("Hash id has no value")
)
