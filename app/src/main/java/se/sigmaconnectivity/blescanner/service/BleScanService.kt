package se.sigmaconnectivity.blescanner.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import org.koin.android.ext.android.inject
import se.sigmaconnectivity.blescanner.Consts
import se.sigmaconnectivity.blescanner.Consts.SCAN_PERIOD_SEC
import se.sigmaconnectivity.blescanner.Consts.SCAN_TIMEOUT_SEC
import se.sigmaconnectivity.blescanner.R
import se.sigmaconnectivity.blescanner.domain.HASH_SIZE_BYTES
import se.sigmaconnectivity.blescanner.domain.HashConverter
import se.sigmaconnectivity.blescanner.domain.model.BLEFeatureState
import se.sigmaconnectivity.blescanner.domain.model.ContactDeviceItem
import se.sigmaconnectivity.blescanner.domain.model.LocationStatus
import se.sigmaconnectivity.blescanner.domain.model.ScanResultItem
import se.sigmaconnectivity.blescanner.domain.usecase.device.AdvertiseTxUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.device.AdvertiseUidUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.device.ScanBleDevicesUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.device.SubscribeForLocationStatusUseCase
import se.sigmaconnectivity.blescanner.extensions.showToast
import se.sigmaconnectivity.blescanner.notification.BleNotificationManager
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
    private val scanBleDevicesUseCase: ScanBleDevicesUseCase by inject()
    private val subscribeForLocationStatusUseCase: SubscribeForLocationStatusUseCase by inject()
    private val advertiseUidUseCase: AdvertiseUidUseCase by inject()
    private val advertiseTxUseCase: AdvertiseTxUseCase by inject()
    private val bleNotificationManager: BleNotificationManager by inject()

    //TODO: Use usecase for this conversion
    private val hashConverter: HashConverter by inject()
    private val compositeDisposable = CompositeDisposable()
    private var scanDisposable: Disposable? = null

    override fun onBind(intent: Intent): IBinder {
        return Binder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startForeground(Consts.NOTIFICATION_ID, bleNotificationManager.createNotification())

        isRunning.set(true)

        startAdv()

        observeLocationStatus()

        return START_NOT_STICKY
    }

    private fun startAdv() {
        advertiseUidUseCase.execute(Consts.SERVICE_USER_HASH_UUID, Consts.MANUFACTURER_ID)
            .mergeWith(advertiseTxUseCase.execute(Consts.SERVICE_TX_UUID))
            .doOnDispose {
                Timber.d("scanLeDevice disposed")
                bleNotificationManager.updateNotification(adv = FeatureStatus.INACTIVE)
            }
            .map { if (it == BLEFeatureState.Started) FeatureStatus.ACTIVE else FeatureStatus.INACTIVE }
            .subscribe(
                { bleNotificationManager.updateNotification(adv = it) },
                {
                    bleNotificationManager.updateNotification(adv = FeatureStatus.INACTIVE)
                    Timber.e(it)
                })
            .addTo(compositeDisposable)
    }

    private fun scanLeDevice(): Disposable =
        Observable.interval(0, SCAN_PERIOD_SEC, TimeUnit.SECONDS)
            .flatMapSingle {
                scanBleDevicesUseCase.execute(
                    Consts.SERVICE_USER_HASH_UUID,
                    SCAN_TIMEOUT_SEC * 1000L
                )
                    .filter {
                        assembleUID(it) != null
                    }
                    .map {
                        val uid = assembleUID(it)
                        checkNotNull(uid)
                        ContactDeviceItem(
                            timestamp = System.currentTimeMillis(),
                            hashId = uid
                        )
                    }
                    .collect(
                        { HashSet() },
                        { items: MutableSet<ContactDeviceItem>, item: ContactDeviceItem ->
                            items.add(
                                item
                            )
                        })
            }
            .doOnDispose {
                Timber.d("scanLeDevice disposed")
                bleNotificationManager.updateNotification(scan = FeatureStatus.INACTIVE)
            }.doOnSubscribe {
                Timber.d("scanLeDevice started")
                bleNotificationManager.updateNotification(scan = FeatureStatus.ACTIVE)
            }.subscribe(
                {
                    scanResultsObserver.onNewResults(it)
                },
                {
                    Timber.d(it, "Device found with error")
                    bleNotificationManager.updateNotification(scan = FeatureStatus.INACTIVE)
                }
            ).addTo(compositeDisposable)

    private fun assembleUID(scanResult: ScanResultItem): String? {
        val results = scanResult.manufacturerSpecificData[Consts.MANUFACTURER_ID]
        return results?.let {
            //TODO: change it to chained rx invocation
            val bytes = ByteBuffer.allocate(8)
                .put(it)
            val hashBytes = bytes.array().sliceArray(0 until HASH_SIZE_BYTES)
            hashConverter.convert(hashBytes)

        }
    }

    override fun onDestroy() {
        isRunning.set(false)
        compositeDisposable.clear()
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
                        showToast("Please enable Location to use ${getString(R.string.app_name)}")
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
}