package se.sigmaconnectivity.blescanner

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.ParcelUuid
import androidx.core.app.NotificationCompat
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.disposables.CompositeDisposable
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.*


class BleScanService() : Service() {

    private val rxBleClient: RxBleClient by inject()
    private val compositeDisposable = CompositeDisposable()
    private var scanStatus = BLEFeatureStatus.INACTIVE
    private var advertiseStatus = BLEFeatureStatus.INACTIVE

    private val bluetoothAdapter by lazy {
        (getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
    }

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }

    private val mAdvertiseCallback: AdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Timber.d("Peripheral advertising started.")
            advertiseStatus = BLEFeatureStatus.ACTIVE
            updateNotification()
        }

        override fun onStartFailure(errorCode: Int) {
            Timber.d("Peripheral advertising failed: $errorCode")
            advertiseStatus = BLEFeatureStatus.INACTIVE
            updateNotification()
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return Binder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startForeground(Consts.NOTIFICATION_ID, createNotification())
        startScan()

        return START_NOT_STICKY
    }

    private fun startScan() {
        scanLeDevice()
        bluetoothAdapter?.let { startAdv(it) } ?: Timber.e("BT not supported")
    }

    private fun startAdv(mBluetoothAdapter: BluetoothAdapter) {
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setConnectable(false)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
            .build()
        val data: AdvertiseData = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(UUID.fromString(Consts.SERVICE_UUID)))
            .build()

        mBluetoothAdapter.bluetoothLeAdvertiser.startAdvertising(settings, data, mAdvertiseCallback)
    }

    private fun scanLeDevice() {
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()
        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(UUID.fromString(Consts.SERVICE_UUID)))
            .build()
        compositeDisposable.add(
            rxBleClient.scanBleDevices(scanSettings, scanFilter)
                .doOnSubscribe {
                    Timber.d("scanLeDevice started")
                    scanStatus = BLEFeatureStatus.ACTIVE
                }
                .doOnDispose { scanStatus = BLEFeatureStatus.INACTIVE }
                .subscribe(
                    {
                        Timber.d("Device found ${it.bleDevice.bluetoothDevice.address}")
                    },
                    {
                        Timber.d("Device found $it")
                    }
                )
        )
    }

    private fun updateNotification() {
        notificationManager?.notify(Consts.NOTIFICATION_ID, createNotification())
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            baseContext,
            0,
            notificationIntent,
            0
        )
        return NotificationCompat.Builder(applicationContext, Consts.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(getString(R.string.notification_content, scanStatus, advertiseStatus))
            )
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        bluetoothAdapter?.bluetoothLeAdvertiser?.stopAdvertising(mAdvertiseCallback).also {
            Timber.d("Advertising stopped")
        }
        stopForeground(true)
        stopSelf()
        super.onDestroy()
    }
}

enum class BLEFeatureStatus {
    ACTIVE,
    INACTIVE
}
