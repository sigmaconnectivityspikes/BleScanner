package se.sigmaconnectivity.blescanner.domain

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import se.sigmaconnectivity.blescanner.domain.entity.Entity

interface ContactRepository {
    fun deleteContact(contact: Entity.Contact): Completable
    fun saveContact(contact: Entity.Contact): Completable
    fun getDevicesCount(): Single<Int>
    fun getContacts(): Observable<List<Entity.Contact>>
    fun getContactByHashIfNotLostOrNew(hash: String): Single<Entity.Contact>
    fun getContactByHash(hash: String): Maybe<Entity.Contact>
    fun getAllContacts(): Maybe<List<Entity.Contact>>
}