package se.sigmaconnectivity.blescanner.device.advertiser

import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import se.sigmaconnectivity.blescanner.domain.model.BLEFeatureState
import se.sigmaconnectivity.blescanner.domain.model.StatusErrorType
import timber.log.Timber

abstract class BleAdvertiser {
    protected val advertisingStatusSubject: BehaviorSubject<BLEFeatureState> =
        BehaviorSubject.createDefault(BLEFeatureState.Stopped)

    protected abstract val bluetoothLeAdvertiser: BluetoothLeAdvertiser?

    private val advertiseCallback: AdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Timber.d("Peripheral advertising started.")
            advertisingStatusSubject.onNext(BLEFeatureState.Started)
        }

        override fun onStartFailure(errorCode: Int) {
            Timber.d("Peripheral advertising failed: $errorCode")
            if (errorCode == ADVERTISE_FAILED_ALREADY_STARTED) {
                advertisingStatusSubject.onNext(BLEFeatureState.Started)
            } else {
                advertisingStatusSubject.onNext(BLEFeatureState.Error(StatusErrorType.ILLEGAL_BLUETOOTH_STATE))
            }
        }
    }

    private val trackScanningStatus: Observable<BLEFeatureState>
        get() = advertisingStatusSubject.hide()

    protected open fun startAdvertising(settings: AdvertiseSettings, data: AdvertiseData): Observable<BLEFeatureState> {
        Timber.d("BT-startAdvertising")
        Timber.d("BT-Advertise data value $data")
        bluetoothLeAdvertiser?.startAdvertising(settings, data, advertiseCallback) ?: run {
            advertisingStatusSubject.onNext(BLEFeatureState.Error(StatusErrorType.ILLEGAL_BLUETOOTH_STATE))
        }
        return trackScanningStatus
    }

    open fun stopAdv() {
        bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        Timber.d("Advertiser stopped")
    }

}