package se.sigmaconnectivity.blescanner.device

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import se.sigmaconnectivity.blescanner.domain.ble.BluetoothStatusRepository
import se.sigmaconnectivity.blescanner.domain.model.BluetoothStatus

/**
 * Assume that this class will be a singleton in DI
 */
class BluetoothStatusRepositoryImpl:
    BluetoothStatusRepository {

    private val statusSubject = PublishSubject.create<BluetoothStatus>()

    override fun getBluetoothStatus(): Observable<BluetoothStatus> {
        return statusSubject
    }

    override fun putBluetoothStatus(status: BluetoothStatus) {
        try {
            statusSubject.onNext(status)
        } catch (ex: Exception) {
            statusSubject.onError(ex)
        }
    }
}