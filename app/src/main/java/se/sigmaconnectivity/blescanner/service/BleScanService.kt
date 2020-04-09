package se.sigmaconnectivity.blescanner.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
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
import se.sigmaconnectivity.blescanner.domain.model.BLEFeatureState
import se.sigmaconnectivity.blescanner.domain.model.LocationStatus
import se.sigmaconnectivity.blescanner.domain.model.ScanResultItem
import se.sigmaconnectivity.blescanner.domain.usecase.device.AdvertiseTxUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.device.AdvertiseUidUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.device.ScanBleDevicesUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.device.SubscribeForLocationStatusUseCase
import se.sigmaconnectivity.blescanner.ui.MainActivity
import se.sigmaconnectivity.blescanner.ui.feature.FeatureStatus
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class BleScanService() : Service() {

    companion object {
        var isRunning = AtomicBoolean(false)
    }

    private val scanResultsObserver: ScanResultsObserver by inject()
    private val scanBleDevicesUseCase: ScanBleDevicesUseCase by inject()
    private val subscribeForLocationStatusUseCase: SubscribeForLocationStatusUseCase by inject()
    private val advertiseUidUseCase: AdvertiseUidUseCase by inject()
    private val advertiseTxUseCase: AdvertiseTxUseCase by inject()

    private val compositeDisposable = CompositeDisposable()
    private var scanStatus = FeatureStatus.INACTIVE
    private var advertiseStatus = FeatureStatus.INACTIVE

    private var scanDisposable: Disposable? = null

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
            bluetoothAdapter?.let { startAdv() } ?: Timber.e("BT not supported")
        }

        observeLocationStatus()

        return START_NOT_STICKY
    }

    private fun startAdv() {
        advertiseUidUseCase.execute(Consts.SERVICE_USER_HASH_UUID, Consts.MANUFACTURER_ID)
            .mergeWith(advertiseTxUseCase.execute(Consts.SERVICE_TX_UUID, Consts.MANUFACTURER_ID))
            .map { if (it == BLEFeatureState.Started) FeatureStatus.ACTIVE else FeatureStatus.INACTIVE }
            .subscribe({ updateNotification(adv = it) }, { Timber.e(it) })
            .addTo(compositeDisposable)
    }

    private fun scanLeDevice(): Disposable =
        Observable.interval(0, SCAN_PERIOD_SEC, TimeUnit.SECONDS)
            .flatMapSingle {
                scanBleDevicesUseCase.execute(
                    listOf(Consts.SERVICE_USER_HASH_UUID, Consts.SERVICE_TX_UUID),
                    SCAN_TIMEOUT_SEC * 1000L
                )
                    .collect(
                        { ArrayList() },
                        { items: MutableList<ScanResultItem>, item: ScanResultItem ->
                            items.add(
                                item
                            )
                        })
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
        stopForeground(true)
        stopSelf()
        super.onDestroy()
    }

    private fun observeLocationStatus() {
        subscribeForLocationStatusUseCase.execute().subscribe(
            {
                Timber.d("Location status: $it")
                when (it) {
                    LocationStatus.NOT_READY -> {
                        showEnableLocationToast()
                        scanDisposable?.dispose()
                    }
                    LocationStatus.READY -> {
                        scanDisposable = scanLeDevice()
                    }
                    else -> throw IllegalStateException("Unknown location status")
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