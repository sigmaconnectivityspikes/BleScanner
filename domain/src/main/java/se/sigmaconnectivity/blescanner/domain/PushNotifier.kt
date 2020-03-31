package se.sigmaconnectivity.blescanner.domain

import io.reactivex.Completable
import io.reactivex.Observable
import se.sigmaconnectivity.blescanner.domain.model.InfectionItem

interface PushNotifier {
    fun notifyInfection(infectionItem: InfectionItem): Completable
    val trackInfectionNotifications: Observable<InfectionItem>
}