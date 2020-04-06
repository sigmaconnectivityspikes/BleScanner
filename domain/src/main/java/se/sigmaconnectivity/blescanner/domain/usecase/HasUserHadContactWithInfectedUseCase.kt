package se.sigmaconnectivity.blescanner.domain.usecase

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Duration
import se.sigmaconnectivity.blescanner.domain.ContactRepository
import se.sigmaconnectivity.blescanner.domain.executor.PostExecutionThread

class HasUserHadContactWithInfectedUseCase(
    private val postExecutionThread: PostExecutionThread,
    private val contactRepository: ContactRepository
) {

    fun execute(hash: String): Single<Boolean> {
        return contactRepository.getContactByHash(hash)
            .map {contact ->
                contact.duration >= RISKY_CONTACT_DURATION.toMillis()
            }.toSingle(false)
            .subscribeOn(Schedulers.io())
            .observeOn(postExecutionThread.scheduler)
    }
}

//TODO: consult value with medical experts:
val RISKY_CONTACT_DURATION = Duration.ofMinutes(2)