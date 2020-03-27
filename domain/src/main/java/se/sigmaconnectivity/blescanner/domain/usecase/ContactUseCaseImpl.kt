package se.sigmaconnectivity.blescanner.domain.usecase

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import se.sigmaconnectivity.blescanner.domain.ContactRepository
import se.sigmaconnectivity.blescanner.domain.entity.Entity
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

    override fun getContactByHashOrNew(hash: String): Single<Entity.Contact> {
        return contactRepository.getContactByHashOrNew(hash)
            .subscribeOn(Schedulers.io())
            .observeOn(postExecutionThread.scheduler)
    }

    override fun getDevicesCount(): Single<Int> {
        return contactRepository.getDevicesCount()
            .subscribeOn(Schedulers.io())
            .observeOn(postExecutionThread.scheduler)
    }

    override fun processContactMatch(hash: String, timestamp: Long): Completable {
        return getContactByHashOrNew(hash)
            .flatMapCompletable { contact: Entity.Contact ->
                val contactToUpdate = contact.apply {
                    lastTimeStamp = timestamp
                }
                saveContact(contactToUpdate)
            }
    }

    override fun processContactLost(hash: String, timestamp: Long): Completable {
        return getContactByHashOrNew(hash)
            .flatMapCompletable { contact: Entity.Contact ->
                val contactToUpdate = contact.apply {
                    totalContactTime = timestamp - lastTimeStamp
                    lastTimeStamp = timestamp
                    contactCounter.inc()
                }
                saveContact(contactToUpdate)
            }
    }
}