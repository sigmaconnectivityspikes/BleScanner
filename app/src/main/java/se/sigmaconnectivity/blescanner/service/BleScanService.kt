package se.sigmaconnectivity.blescanner.service

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
import com.polidea.rxandroidble2.scan.ScanCallbackType
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.disposables.CompositeDisposable
import org.koin.android.ext.android.inject
import se.sigmaconnectivity.blescanner.Consts
import se.sigmaconnectivity.blescanner.R
import org.threeten.bp.Duration
import se.sigmaconnectivity.blescanner.domain.feature.FeatureStatus
import se.sigmaconnectivity.blescanner.domain.usecase.ContactUseCase
import se.sigmaconnectivity.blescanner.ui.MainActivity
import timber.log.Timber
import java.nio.ByteBuffer
import java.util.*

class BleScanService() : Service() {

    private val rxBleClient: RxBleClient by inject()
    private val contactUseCase: ContactUseCase by inject()
    private val sharedPrefs: SharedPrefs by inject()
    private val compositeDisposable = CompositeDisposable()
    private var scanStatus = FeatureStatus.INACTIVE
    private var advertiseStatus = FeatureStatus.INACTIVE

    private val bluetoothAdapter by lazy {
        (getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
    }

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }

    private val mAdvertiseCallback: AdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Timber.d("Peripheral advertising started.")
            advertiseStatus = FeatureStatus.ACTIVE
            updateNotification()
        }

        override fun onStartFailure(errorCode: Int) {
            Timber.d("Peripheral advertising failed: $errorCode")
            advertiseStatus = FeatureStatus.INACTIVE
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
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
            .setConnectable(false)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
            .build()

        val userId = sharedPrefs.getUserId() ?: throw IllegalArgumentException("Empty user id")
        val data: AdvertiseData = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .addServiceData(
                ParcelUuid(UUID.fromString(Consts.SERVICE_UUID)),
                generateUID(userId)
            )
            .build()

        Timber.d("$data")

        mBluetoothAdapter.bluetoothLeAdvertiser.startAdvertising(settings, data, mAdvertiseCallback)
    }

    private fun scanLeDevice() {
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH or ScanSettings.CALLBACK_TYPE_MATCH_LOST)
            .build()
        val scanFilter = ScanFilter.Builder()
            .setServiceData(
                ParcelUuid(UUID.fromString(Consts.SERVICE_UUID)),
                ByteBuffer.allocate(8).array(),
                ByteBuffer.allocate(8).array()
            )
            .build()
        compositeDisposable.add(
            rxBleClient.scanBleDevices(scanSettings, scanFilter)
                .doOnSubscribe {
                    Timber.d("scanLeDevice started")
                    scanStatus = FeatureStatus.ACTIVE
                }
                .doOnDispose { scanStatus = FeatureStatus.INACTIVE }
                .subscribe(
                    {scanResult ->
                        val timestampMillis = Duration.ofNanos(scanResult.timestampNanos).toMillis()
                        assembleUID(scanResult)?.let {
                            if (scanResult.callbackType == ScanCallbackType.CALLBACK_TYPE_FIRST_MATCH) {
                                processFirstMatch(it, timestampMillis)
                            } else {
                                processMatchLost(it, timestampMillis)
                            }
                        } ?: Timber.e("Can not assemble UID")
                    },
                    {
                        Timber.d("Device found with error \n $it")
                    }
                )
        )
    }

    private fun processFirstMatch(contactHash: String, timestamp: Long) {
        Timber.d("CALLBACK_TYPE_FIRST_MATCH: $contactHash")
        compositeDisposable.add(
            contactUseCase.processContactMatch(contactHash, timestamp)
                .subscribe({
                    Timber.d("processContactMatch() SUCCESS")
                }, {
                    Timber.e("processContactMatch() FAILED \n $it")
                })
        )
    }

    private fun processMatchLost(contactHash: String, timestamp: Long) {
        Timber.d("CALLBACK_TYPE_MATCH_LOST: $contactHash")
        compositeDisposable.add(
            contactUseCase.processContactLost(contactHash, timestamp)
                .subscribe({
                    Timber.d("processContactLost() SUCCESS")
                }, {
                    Timber.d("processContactLost() FAILED \n $it")
                })
        )
    }

    private fun generateUID(userId: Long): ByteArray {
        val byteBuffer = ByteBuffer.allocate(8).apply {
            putLong(userId)
        }
        return byteBuffer.array()
    }

    private fun assembleUID(scanResult: ScanResult): String? {
        return scanResult.scanRecord.getServiceData(ParcelUuid.fromString(Consts.SERVICE_UUID))
            ?.let {
                String(it, Charsets.UTF_8)
            }
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
