package se.sigmaconnectivity.blescanner.domain.usecase

import io.reactivex.Completable
import io.reactivex.Single
import se.sigmaconnectivity.blescanner.domain.entity.Entity

interface ContactUseCase {
    fun saveContact(contact: Entity.Contact): Completable
    fun deleteContact(contact: Entity.Contact): Completable
    fun getContactByHashOrNew(hash: String): Single<Entity.Contact>
    fun getContactsCount(): Single<Int>
    fun getAllContacts(): Single<List<Entity.Contact>>
    fun processContactMatch(hash: String, timestamp: Long): Completable
    fun processContactLost(hash: String, timestamp: Long): Completable
}