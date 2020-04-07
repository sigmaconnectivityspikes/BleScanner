package se.sigmaconnectivity.blescanner.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.ParcelUuid
import android.widget.Toast
import androidx.core.app.NotificationCompat
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import org.koin.android.ext.android.inject
import se.sigmaconnectivity.blescanner.Consts
import se.sigmaconnectivity.blescanner.Consts.SCAN_PERIOD_SEC
import se.sigmaconnectivity.blescanner.Consts.SCAN_TIMEOUT_SEC
import se.sigmaconnectivity.blescanner.R
import se.sigmaconnectivity.blescanner.device.BLEFeatureState
import se.sigmaconnectivity.blescanner.device.BluetoothScanner
import se.sigmaconnectivity.blescanner.device.LocationStatus
import se.sigmaconnectivity.blescanner.device.LocationStatusRepository
import se.sigmaconnectivity.blescanner.device.advertiser.BluetoothTxAdvertiser
import se.sigmaconnectivity.blescanner.device.advertiser.BluetoothUIDAdvertiser
import se.sigmaconnectivity.blescanner.domain.HASH_SIZE_BYTES
import se.sigmaconnectivity.blescanner.domain.HashConverter
import se.sigmaconnectivity.blescanner.domain.isValidChecksum
import se.sigmaconnectivity.blescanner.domain.model.ScanResultItem
import se.sigmaconnectivity.blescanner.domain.toChecksum
import se.sigmaconnectivity.blescanner.domain.toHash
import se.sigmaconnectivity.blescanner.domain.usecase.GetUserIdHashUseCase
import se.sigmaconnectivity.blescanner.ui.MainActivity
import se.sigmaconnectivity.blescanner.ui.feature.FeatureStatus
import timber.log.Timber
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class BleScanService() : Service() {

    companion object {
        var isRunning = AtomicBoolean(false)
    }

    private val scanResultsObserver: ScanResultsObserver by inject()
    private val getUserIdHashUseCase: GetUserIdHashUseCase by inject()
    private val bluetoothScanner: BluetoothScanner by inject()
    private val locationStatusRepository: LocationStatusRepository by inject()

    //TODO: Use usecase for this conversion
    private val hashConverter: HashConverter by inject()
    private val compositeDisposable = CompositeDisposable()
    private var scanStatus = FeatureStatus.INACTIVE
    private var advertiseStatus = FeatureStatus.INACTIVE

    private var scanDisposable: Disposable? = null

    private val bluetoothUIDAdvertiser: BluetoothUIDAdvertiser by inject()
    private val bluetoothTxAdvertiser: BluetoothTxAdvertiser by inject()

    private val bluetoothAdapter by lazy {
        (getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
    }

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }

    private val mAdvertiseCallback: AdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Timber.d("Peripheral advertising started.")
            updateNotification(adv = FeatureStatus.ACTIVE)
        }

        override fun onStartFailure(errorCode: Int) {
            Timber.d("Peripheral advertising failed: $errorCode")
            if (errorCode == ADVERTISE_FAILED_ALREADY_STARTED) {
                updateNotification(adv = FeatureStatus.ACTIVE)
            } else {
                updateNotification(adv = FeatureStatus.INACTIVE)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return Binder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startForeground(Consts.NOTIFICATION_ID, createNotification())

        isRunning.set(true)

        if (advertiseStatus == FeatureStatus.INACTIVE) {
            startAdvertising()
        }

        observeLocationStatus()

        return START_NOT_STICKY
    }

    private fun startAdvertising() {
        //TODO should we separate state of advertisers in notification?
        getUserIdHashUseCase.execute()
            .map { it.toHash() }
            .flatMapObservable { userUidHash ->
                val buffer = ByteBuffer.wrap(userUidHash + userUidHash.toChecksum()).array()
                bluetoothUIDAdvertiser.startAdvertising(
                    UUID.fromString(Consts.SERVICE_USER_HASH_UUID),
                    Consts.MANUFACTURER_ID,
                    buffer
                )
            }
            .map { if (it == BLEFeatureState.Started) FeatureStatus.ACTIVE else FeatureStatus.INACTIVE }
            .subscribe { updateNotification(adv = it) }
            .addTo(compositeDisposable)

        bluetoothTxAdvertiser.apply {
            startAdvertising(UUID.fromString(Consts.SERVICE_TX_UUID))
                .map { if (it == BLEFeatureState.Started) FeatureStatus.ACTIVE else FeatureStatus.INACTIVE }
                .subscribe { updateNotification(adv = it) }
                .addTo(compositeDisposable)
        }
    }

    private fun scanLeDevice(): Disposable {

        return Observable.interval(0, SCAN_PERIOD_SEC, TimeUnit.SECONDS)
            .flatMapSingle {
                Timber.d("BT- next scan")
                bluetoothScanner.scanBleDevicesWithTimeout(
                    ParcelUuid.fromString(Consts.SERVICE_TX_UUID),
                    SCAN_TIMEOUT_SEC * 1000L
                )
                    .filter {
                        assembleUID(it) != null
                    }
                    .map {
                        val uid = assembleUID(it)
                        checkNotNull(uid)
                        ScanResultItem(
                            timestamp = System.currentTimeMillis(),
                            hashId = uid
                        )
                    }
                    .collect(
                        { HashSet() },
                        { items: MutableSet<ScanResultItem>, item: ScanResultItem -> items.add(item) })
            }
            .doOnDispose {
                Timber.d("scanLeDevice disposed")
                updateNotification(scan = FeatureStatus.INACTIVE)
            }.doOnSubscribe {
                Timber.d("scanLeDevice started")
                updateNotification(scan = FeatureStatus.ACTIVE)
            }.subscribe(
                {
                    scanResultsObserver.onNewResults(it)
                },
                {
                    Timber.d(it, "Device found with error")
                    updateNotification(scan = FeatureStatus.INACTIVE)
                }
            )
    }

    private fun assembleUID(scanResult: ScanResult): String? {
        val results = scanResult.scanRecord?.getManufacturerSpecificData(Consts.MANUFACTURER_ID)
        return results?.let {
            //TODO: change it to chained rx invocation
            val bytes = ByteBuffer.allocate(8)
                .put(it)
            val hashBytes = bytes.array().sliceArray(0 until HASH_SIZE_BYTES)
            val checksum = bytes.array()[HASH_SIZE_BYTES]
            if (hashBytes.isValidChecksum(checksum)) {
                val data = hashConverter.convert(hashBytes).blockingGet()
                data
            } else {
                null
            }
        }
    }

    private fun updateNotification(
        scan: FeatureStatus = scanStatus,
        adv: FeatureStatus = advertiseStatus
    ) {
        scanStatus = scan
        advertiseStatus = adv
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
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.app_name))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        getString(
                            R.string.notification_content,
                            getString(scanStatus.nameRes),
                            getString(advertiseStatus.nameRes)
                        )
                    )
            )
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onDestroy() {
        isRunning.set(false)
        compositeDisposable.clear()
        if (scanDisposable?.isDisposed == false) scanDisposable?.dispose()
        bluetoothAdapter?.bluetoothLeAdvertiser?.stopAdvertising(mAdvertiseCallback).also {
            Timber.d("Advertising stopped")
            updateNotification(adv = FeatureStatus.INACTIVE)
        }
        scanResultsObserver.onClear()
        bluetoothUIDAdvertiser.stopAdv()
        bluetoothTxAdvertiser.stopAdv()
        stopForeground(true)
        stopSelf()
        super.onDestroy()
    }

    private fun observeLocationStatus() {
        locationStatusRepository.trackLocationStatus.subscribe(
            {
                Timber.d("-BT- Location status: $it")
                when (it) {
                    LocationStatus.NOT_READY -> {
                        showEnableLocationToast()
                        scanDisposable?.dispose()
                    }
                    LocationStatus.READY -> {
                        scanDisposable = scanLeDevice()
                    }
                }
            }, {
                Timber.e(it)
            }
        ).addTo(compositeDisposable)
    }

    private fun showEnableLocationToast() {
        Toast.makeText(
            this,
            "Please enable Location to use ${getString(R.string.app_name)}",
            Toast.LENGTH_LONG
        ).show()
    }
}