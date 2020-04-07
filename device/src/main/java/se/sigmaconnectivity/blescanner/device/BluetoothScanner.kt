package se.sigmaconnectivity.blescanner.device

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
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val bleScanner: BluetoothLeScanner? by lazy(LazyThreadSafetyMode.NONE) {
        bluetoothAdapter?.bluetoothLeScanner
    }
    private val scanResultsSubject: PublishSubject<ScanResultWrapper> = PublishSubject.create()

    private val scanningStatusSubject: BehaviorSubject<BLEScanState> = BehaviorSubject.createDefault(
        BLEScanState.Stopped
    )



    private fun startScan(serviceUuid: ParcelUuid) {
        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(serviceUuid)
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        bleScanner?.let {
            it.startScan(mutableListOf(scanFilter), settings, scanCallback)
            scanningStatusSubject.onNext(BLEScanState.Started)
        } ?: run { onScanError(StatusErrorType.ILLEGAL_BLUETOOTH_STATE) }
    }

    private fun stopScan() {
        bleScanner?.stopScan(scanCallback)
        scanningStatusSubject.onNext(BLEScanState.Stopped)
    }

    private fun onScanError(error: StatusErrorType) {
        scanResultsSubject.onNext(
            ScanResultWrapper.ScanResultFailure(
                IllegalStateException("Not ready to start scanning")
            )
        )
        scanningStatusSubject.onNext(BLEScanState.Error(error))
    }

    fun scanBleDevicesWithTimeout(serviceUuid: ParcelUuid, timeoutMillis: Long): Observable<ScanResult> =
        scanBleDevices(serviceUuid).takeUntil(
            Observable.timer(timeoutMillis, TimeUnit.MILLISECONDS)
        )

    val trackScanningStatus: Observable<BLEScanState>
        get() = scanningStatusSubject.hide()

    private fun scanBleDevices(serviceUuid: ParcelUuid): Observable<ScanResult> =
        scanResultsSubject
            .hide()
            .doOnSubscribe {
                Timber.d("-BT- startScan ")
                startScan(serviceUuid)
            }.doOnDispose {
                Timber.d("-BT- stopScan ")
                stopScan()
            }.map {result ->
                when(result) {
                    is ScanResultWrapper.ScanResultFailure -> throw result.error
                    is ScanResultWrapper.ScanResultSuccess -> result.scanResult
                }
            }

    private val scanCallback = object: ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            scanResultsSubject.onNext(
                ScanResultWrapper.ScanResultSuccess(
                    result
                )
            )
        }
    }

    private sealed class ScanResultWrapper {
        class ScanResultSuccess(val scanResult: ScanResult) : ScanResultWrapper()
        class ScanResultFailure(val error: Throwable) : ScanResultWrapper()

    }
}

