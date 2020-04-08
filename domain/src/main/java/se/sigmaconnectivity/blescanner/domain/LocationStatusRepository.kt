package se.sigmaconnectivity.blescanner.domain

import io.reactivex.Observable
import se.sigmaconnectivity.blescanner.domain.model.LocationStatus

interface LocationStatusRepository {
    val trackLocationStatus: Observable<LocationStatus>
}