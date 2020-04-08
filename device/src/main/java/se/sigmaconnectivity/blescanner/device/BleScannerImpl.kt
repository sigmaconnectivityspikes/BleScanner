package se.sigmaconnectivity.blescanner.device

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import se.sigmaconnectivity.blescanner.device.converters.toDomainItem
import se.sigmaconnectivity.blescanner.domain.BleScanner
import se.sigmaconnectivity.blescanner.domain.model.BLEScanState
import se.sigmaconnectivity.blescanner.domain.model.ScanResultItem
import se.sigmaconnectivity.blescanner.domain.model.StatusErrorType
import timber.log.Timber
import java.util.concurrent.TimeUnit

class BleScannerImpl(private val context: Context) :
    BleScanner {

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

    private fun startScan(serviceUuid: String) {
        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid.fromString(serviceUuid))
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        bleScanner?.let {
            try {
                it.startScan(mutableListOf(scanFilter), settings, scanCallback)
            } catch (e: Exception) {
                onScanError(e)
            }
            scanningStatusSubject.onNext(BLEScanState.Started)
        } ?: run { onScanError(java.lang.IllegalStateException("Couldn't get BLE scanner")) }
    }

    private fun stopScan() {
        try {
            bleScanner?.stopScan(scanCallback)
        } catch (e: Exception) {
            onScanError(e)
            return
        }
        scanningStatusSubject.onNext(BLEScanState.Stopped)
    }

    private fun onScanError(error: Throwable) {
        scanResultsSubject.onNext(
            ScanResultWrapper.ScanResultFailure(
                IllegalStateException("Not ready to start scanning")
            )
        )
        val errorType = when (error) {
            is IllegalStateException -> StatusErrorType.ILLEGAL_BLUETOOTH_STATE
            else -> StatusErrorType.UNKNOWN
        }
        scanningStatusSubject.onNext(BLEScanState.Error(errorType))
    }

    override fun scanBleDevicesWithTimeout(serviceUuid: String, timeoutMillis: Long): Observable<ScanResultItem> =
        scanBleDevices(serviceUuid).takeUntil(
            Observable.timer(timeoutMillis, TimeUnit.MILLISECONDS)
        )

    override val trackScanningStatus: Observable<BLEScanState>
        get() = scanningStatusSubject.hide()

    private fun scanBleDevices(serviceUuid: String): Observable<ScanResultItem> =
        scanResultsSubject
            .hide()
            .doOnSubscribe {
                Timber.d("scanBleDevices started...")
                startScan(serviceUuid)
            }.doOnDispose {
                Timber.d("scanBleDevices done...")
                stopScan()
            }.map {result ->
                when(result) {
                    is ScanResultWrapper.ScanResultFailure -> throw result.error
                    is ScanResultWrapper.ScanResultSuccess -> result.scanResult.toDomainItem()
                }
            }.doOnError {
                Timber.e(it, "scanBleDevices error")
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

