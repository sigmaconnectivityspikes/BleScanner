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

    private val currentContacts = hashMapOf<String, Long>()

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

    override fun getContactsCount(): Single<Int> {
        return contactRepository.getDevicesCount()
            .subscribeOn(Schedulers.io())
            .observeOn(postExecutionThread.scheduler)
    }

    override fun getAllContacts(): Single<List<Entity.Contact>> {
        return contactRepository.getAllContacts()
            .toSingle(emptyList())
            .subscribeOn(Schedulers.io())
            .observeOn(postExecutionThread.scheduler)
    }

    override fun processContactMatch(hash: String, timestamp: Long): Completable {
        return Completable.fromAction {
            currentContacts[hash] = timestamp
        }
    }

    override fun processContactLost(hash: String, timestamp: Long): Completable {
        return Completable.fromAction {
            saveContact(
                Entity.Contact(
                    hash = hash,
                    lastTimeStamp = currentContacts[hash]!!,
                    totalContactTime = timestamp - currentContacts[hash]!!
                )
            )
        }
    }
}