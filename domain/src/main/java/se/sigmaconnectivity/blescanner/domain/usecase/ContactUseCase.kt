package se.sigmaconnectivity.blescanner.domain.usecase

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import se.sigmaconnectivity.blescanner.domain.entity.Entity

interface ContactUseCase {
    fun saveContact(contact: Entity.Contact): Completable
    fun deleteContact(contact: Entity.Contact): Completable
    fun getContactByHashIfNotLostOrNew(hash: String): Single<Entity.Contact>
    fun getDevicesCount(): Single<Int>
    fun getContacts(): Observable<List<Entity.Contact>>
    fun processContactMatch(hash: String, timestamp: Long): Completable
    fun processContactLost(hash: String, timestamp: Long): Completable
}