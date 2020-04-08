package se.sigmaconnectivity.blescanner.device.advertiser

import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import se.sigmaconnectivity.blescanner.domain.ble.AdvertiserData
import se.sigmaconnectivity.blescanner.domain.ble.BleAdvertiser
import se.sigmaconnectivity.blescanner.domain.model.BLEFeatureState
import se.sigmaconnectivity.blescanner.domain.model.StatusErrorType
import timber.log.Timber

abstract class BleAdvertiserImpl: BleAdvertiser {

    protected val advertisingStatusSubject: BehaviorSubject<BLEFeatureState> =
        BehaviorSubject.createDefault(BLEFeatureState.Stopped)

    protected abstract val bluetoothLeAdvertiser: BluetoothLeAdvertiser?
    protected abstract val settings: AdvertiseSettings

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

    private val trackAdvertisingStatus: Observable<BLEFeatureState>
        get() = advertisingStatusSubject.hide()

    override fun startAdvertising(advertiserData: AdvertiserData): Observable<BLEFeatureState> {
        val data = buildData(advertiserData)
        Timber.d("BT-Advertise data value $advertiserData")
        bluetoothLeAdvertiser?.startAdvertising(settings, data, advertiseCallback) ?: run {
            advertisingStatusSubject.onNext(BLEFeatureState.Error(StatusErrorType.ILLEGAL_BLUETOOTH_STATE))
        }
        Timber.d("BT-Advertiser started")
        return trackAdvertisingStatus
    }

    protected abstract fun buildData(data: AdvertiserData): AdvertiseData

    override fun stopAdvertising() {
        bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        Timber.d("Advertiser stopped")
    }

}