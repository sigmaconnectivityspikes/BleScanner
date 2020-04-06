package se.sigmaconnectivity.blescanner.domain.usecase

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import se.sigmaconnectivity.blescanner.domain.ContactRepository
import se.sigmaconnectivity.blescanner.domain.entity.Entity
import se.sigmaconnectivity.blescanner.domain.entity.STATUS_LOST
import se.sigmaconnectivity.blescanner.domain.entity.STATUS_MATCHED
import se.sigmaconnectivity.blescanner.domain.executor.PostExecutionThread

class ContactUseCaseImpl(
    private val postExecutionThread: PostExecutionThread,
    private val contactRepository: ContactRepository
) : ContactUseCase {

    override fun saveContact(contact: Entity.Contact): Completable {
        return contactRepository.saveContact(contact)
            .subscribeOn(Schedulers.io())
            .observeOn(postExecutionThread.scheduler)
    }

    override fun deleteContact(contact: Entity.Contact): Completable {
        return contactRepository.deleteContact(contact)
            .subscribeOn(Schedulers.io())
            .observeOn(postExecutionThread.scheduler)
    }

    override fun getContactByHashIfNotLostOrNew(hash: String): Single<Entity.Contact> {
        return contactRepository.getContactByHashIfNotLostOrNew(hash)
            .subscribeOn(Schedulers.io())
            .observeOn(postExecutionThread.scheduler)
    }

    override fun getDevicesCount(): Single<Int> {
        return contactRepository.getDevicesCount()
            .subscribeOn(Schedulers.io())
            .observeOn(postExecutionThread.scheduler)
    }

    override fun getContacts(): Observable<List<Entity.Contact>> {
        return contactRepository.getContacts()
            .subscribeOn(Schedulers.io())
            .observeOn(postExecutionThread.scheduler)
    }

    override fun processContactMatch(hash: String, timestamp: Long): Completable {
        return getContactByHashIfNotLostOrNew(hash)
            .flatMapCompletable { contact: Entity.Contact ->
                val contactToUpdate = contact.apply {
                    status = STATUS_MATCHED
                    this.timestamp = if (this.timestamp > 0) this.timestamp else timestamp
                    lostTimestamp = timestamp
                }
                saveContact(contactToUpdate)
            }
    }

    override fun processContactLost(hash: String, timestamp: Long): Completable {
        return getContactByHashIfNotLostOrNew(hash)
            .flatMapCompletable { contact: Entity.Contact ->
                val contactToUpdate = contact.apply {
                    status = STATUS_LOST
                    lostTimestamp = timestamp
                    duration = (lostTimestamp - this.timestamp) / 1000
                }
                saveContact(contactToUpdate)
            }
    }
}