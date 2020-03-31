package se.sigmaconnectivity.blescanner.data

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import se.sigmaconnectivity.blescanner.domain.PushNotifier
import se.sigmaconnectivity.blescanner.domain.model.InfectionItem

class PushNotifierImpl : PushNotifier {
    private val infectionsSubject: PublishSubject<InfectionItem> = PublishSubject.create()

    override fun notifyInfection(infectionItem: InfectionItem) = Completable.fromAction{
        infectionsSubject.onNext(infectionItem)
    }

    override val trackInfectionNotifications: Observable<InfectionItem> = infectionsSubject.hide()

}