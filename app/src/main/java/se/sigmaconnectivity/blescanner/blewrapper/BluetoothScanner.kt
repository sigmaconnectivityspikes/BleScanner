package se.sigmaconnectivity.blescanner.blewrapper

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit


class BluetoothScanner(private val context: Context) {
    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val bleScanner: BluetoothLeScanner? by lazy(LazyThreadSafetyMode.NONE) {
        bluetoothAdapter?.bluetoothLeScanner
    }
    private val scanResultsSubject: PublishSubject<ScanResultWrapper> = PublishSubject.create()

    private val statusSubject: BehaviorSubject<BLEScanState> = BehaviorSubject.create()

    fun scanBleDevicesWithTimeout(
        serviceUuid: ParcelUuid,
        timeoutMillis: Long
    ): Observable<ScanResult> =
        scanBleDevices(serviceUuid).takeUntil(
            Observable.timer(timeoutMillis, TimeUnit.MILLISECONDS)
        )

    val trackStatus: Observable<BLEScanState>
        get() = statusSubject.hide()

    private fun scanBleDevices(serviceUuid: ParcelUuid): Observable<ScanResult> =
        scanResultsSubject
            .hide()
            .doOnSubscribe {
                Timber.d("-BT- startScan ")
                startScan(serviceUuid)
            }.doOnDispose {
                Timber.d("-BT- stopScan ")
                stopScan()
            }.map { result ->
                when (result) {
                    is ScanResultWrapper.ScanResultFailure -> throw result.error
                    is ScanResultWrapper.ScanResultSuccess -> result.scanResult
                }
            }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            scanResultsSubject.onNext(ScanResultWrapper.ScanResultSuccess(result))
        }
    }

    private fun startScan(serviceUuid: ParcelUuid) {
        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(serviceUuid)
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        bleScanner?.let {
            it.startScan(mutableListOf(scanFilter), settings, scanCallback)
            statusSubject.onNext(BLEScanState.Started)
        } ?: run { onScanError(StatusErrorType.ILLEGAL_BLUETOOTH_STATE) }
    }

    private fun stopScan() {
        bleScanner?.stopScan(scanCallback)
        statusSubject.onNext(BLEScanState.Ready)
    }

    private fun onScanError(error: StatusErrorType) {
        scanResultsSubject.onNext(
            ScanResultWrapper.ScanResultFailure(IllegalStateException("Problem to start scanning"))
        )
        statusSubject.onNext(BLEScanState.Error(error))
    }

    private sealed class ScanResultWrapper {
        class ScanResultSuccess(val scanResult: ScanResult) : ScanResultWrapper()
        class ScanResultFailure(val error: Throwable) : ScanResultWrapper()
    }
}

