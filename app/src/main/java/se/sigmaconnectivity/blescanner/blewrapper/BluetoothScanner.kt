package se.sigmaconnectivity.blescanner.blewrapper

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import io.reactivex.Observable
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
    private val scanResultsSubject: PublishSubject<ScanResult> = PublishSubject.create()

    fun scanBleDevicesWithTimeout(serviceUuid: ParcelUuid, timeoutMillis: Long): Observable<ScanResult> =
        scanBleDevices(serviceUuid).takeUntil(
            Observable.timer(timeoutMillis, TimeUnit.MILLISECONDS)
        )

    private fun scanBleDevices(serviceUuid: ParcelUuid): Observable<ScanResult> =
        scanResultsSubject
            .hide()
            .doOnSubscribe {
                Timber.d("-BT- startScan ")
                startScan(serviceUuid)
            }.doOnDispose {
                Timber.d("-BT- stopScan ")
                stopScan()
            }

    private val scanCallback =  object: ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            scanResultsSubject.onNext(result)
        }
    }

    private fun startScan(serviceUuid: ParcelUuid) {
        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(serviceUuid)
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        bleScanner?.startScan(mutableListOf(scanFilter), settings, scanCallback)
            ?: throw IllegalArgumentException("Scanning error")
    }

    private fun stopScan() {
        bleScanner?.stopScan(scanCallback)
    }
}