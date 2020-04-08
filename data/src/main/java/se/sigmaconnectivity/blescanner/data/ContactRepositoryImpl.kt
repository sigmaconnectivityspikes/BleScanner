package se.sigmaconnectivity.blescanner.data

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import se.sigmaconnectivity.blescanner.data.db.ContactDao
import se.sigmaconnectivity.blescanner.data.mapper.dataToDomain
import se.sigmaconnectivity.blescanner.data.mapper.domainToData
import se.sigmaconnectivity.blescanner.domain.ContactRepository
import se.sigmaconnectivity.blescanner.domain.entity.Entity
import timber.log.Timber

class ContactRepositoryImpl(private val contactDao: ContactDao) : ContactRepository {
    override fun getContactByHashIfNotLostOrNew(hash: String): Single<Entity.Contact> {
        return Single.fromCallable {
            contactDao.getContactByHashIfNotLost(hash)?.dataToDomain()
                ?: Entity.Contact(name = hash)
        }
    }

    override fun getContactByHash(hash: String): Maybe<Entity.Contact> {
        return Maybe.fromCallable {
            contactDao.getContactByHash(hash)?.dataToDomain()
        }
    }

    override fun deleteContact(contact: Entity.Contact): Completable {
        return Completable.fromAction { contactDao.delete(contact.domainToData()) }
    }

    override fun saveContact(contact: Entity.Contact): Completable {
        return Completable.fromAction {
            Timber.d("saveContact $contact")
            if (contact.id > 0) {
                contactDao.update(contact.domainToData())
            } else {
                contactDao.insertContact(contact.domainToData())
            }
        }
    }

    override fun getDevicesCount(): Single<Int> {
        return Single.fromCallable { contactDao.count() }
    }

    override fun getContacts(): Observable<List<Entity.Contact>> {
        return Observable.fromCallable {
            contactDao.getContacts().map { it.dataToDomain() }
        }
    }
}