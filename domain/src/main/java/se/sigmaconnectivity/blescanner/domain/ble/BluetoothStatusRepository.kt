package se.sigmaconnectivity.blescanner.domain.ble

import io.reactivex.Observable
import se.sigmaconnectivity.blescanner.domain.model.BluetoothStatus

interface BluetoothStatusRepository {

    fun getBluetoothStatus(): Observable<BluetoothStatus>

    fun putBluetoothStatus(status: BluetoothStatus)
}