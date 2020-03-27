package se.sigmaconnectivity.blescanner.data.mapper

import se.sigmaconnectivity.blescanner.data.entity.Contact
import se.sigmaconnectivity.blescanner.domain.entity.Entity

fun Entity.Contact.domainToData() = Contact(
    hash, lastTimeStamp, contactCounter, totalContactTime
)