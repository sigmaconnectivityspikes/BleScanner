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
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.exceptions.BleScanException
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import org.koin.android.ext.android.inject
import se.sigmaconnectivity.blescanner.Consts
import se.sigmaconnectivity.blescanner.Consts.SCAN_PERIOD_SEC
import se.sigmaconnectivity.blescanner.Consts.SCAN_TIMEOUT_SEC
import se.sigmaconnectivity.blescanner.R
import se.sigmaconnectivity.blescanner.data.HASH_SIZE_BYTES
import se.sigmaconnectivity.blescanner.data.isValidChecksum
import se.sigmaconnectivity.blescanner.data.toChecksum
import se.sigmaconnectivity.blescanner.data.toHash
import se.sigmaconnectivity.blescanner.domain.HashConverter
import se.sigmaconnectivity.blescanner.domain.model.ScanResultItem
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

    private val rxBleClient: RxBleClient by inject()
    private val scanResultsObserver: ScanResultsObserver by inject()
    private val getUserIdHashUseCase: GetUserIdHashUseCase by inject()

    //TODO: Use usecase for this conversion
    private val hashConverter: HashConverter by inject()
    private val compositeDisposable = CompositeDisposable()
    private var scanStatus = FeatureStatus.INACTIVE
    private var advertiseStatus = FeatureStatus.INACTIVE

    private lateinit var scanDisposable: Disposable

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

        // handle the case when service is started multiply times
        if (scanStatus == FeatureStatus.INACTIVE) {
            scanDisposable = scanLeDevice()
        }

        if (advertiseStatus == FeatureStatus.INACTIVE) {
            bluetoothAdapter?.let { startAdv(it) } ?: Timber.e("BT not supported")
        }

        observeStatus()

        return START_NOT_STICKY
    }

    private fun startAdv(mBluetoothAdapter: BluetoothAdapter) {
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
            .setConnectable(false)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
            .build()

        getUserIdHashUseCase.execute().subscribe({ userUid ->
            val serviceUUID = UUID.fromString(Consts.SERVICE_UUID)
            val userIdHash = userUid.toHash()
            val buffer = ByteBuffer.wrap(userIdHash + userIdHash.toChecksum())
            val data: AdvertiseData = AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .addManufacturerData(Consts.MANUFACTURER_ID, buffer.array())
                .addServiceUuid(ParcelUuid(serviceUUID))
                .build()

            Timber.d("Advertise data value $data")
            Timber.d("BT- Advertise data: ${hashConverter.convert(userIdHash).blockingGet()}")

            mBluetoothAdapter.bluetoothLeAdvertiser.startAdvertising(
                settings,
                data,
                mAdvertiseCallback
            )
        }, {
            Timber.e(it)
        }).addTo(compositeDisposable)

    }

    private fun doScan(scanSettings: ScanSettings, scanFilter: ScanFilter, timeoutS: Long) =
        rxBleClient.scanBleDevices(scanSettings, scanFilter).takeUntil(
            Observable.timer(timeoutS, TimeUnit.SECONDS)
        ).doOnError {
            Timber.w(it, "-BT-  Scan  error")
        }

    private fun scanLeDevice(): Disposable {
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .build()
        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid.fromString(Consts.SERVICE_UUID))
            .build()
        return Observable.interval(0, SCAN_PERIOD_SEC, TimeUnit.SECONDS)
            .flatMapSingle {
                Timber.d("BT- next scan")
                doScan(scanSettings, scanFilter, SCAN_TIMEOUT_SEC)
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
                    if (it is BleScanException) {
                        updateNotification(scan = FeatureStatus.INACTIVE)
                    }
                }
            )
    }

    private fun assembleUID(scanResult: ScanResult): String? {
        val results = scanResult.scanRecord.getManufacturerSpecificData(Consts.MANUFACTURER_ID)
        return results?.let {
            //TODO: change it to chained rx invocation
            val bytes = ByteBuffer.allocate(8)
                .put(it)
            val hashBytes = bytes.array().sliceArray(0 until HASH_SIZE_BYTES )
            val checksum = bytes.array()[HASH_SIZE_BYTES]
            if (hashBytes.isValidChecksum(checksum)) {
                val data = hashConverter.convert(hashBytes).blockingGet()
                data
            } else {
                null
            }
        }
    }

    private fun updateNotification(scan: FeatureStatus = scanStatus, adv: FeatureStatus = advertiseStatus) {
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
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(getString(R.string.notification_content, getString(scanStatus.nameRes), getString(advertiseStatus.nameRes)))
            )
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onDestroy() {
        isRunning.set(false)
        compositeDisposable.clear()
        if (!scanDisposable.isDisposed) scanDisposable.dispose()
        bluetoothAdapter?.bluetoothLeAdvertiser?.stopAdvertising(mAdvertiseCallback).also {
            Timber.d("Advertising stopped")
        }
        scanResultsObserver.onClear()
        stopForeground(true)
        stopSelf()
        super.onDestroy()
    }

    private fun observeStatus() {
        if (rxBleClient.state == RxBleClient.State.LOCATION_SERVICES_NOT_ENABLED) {
            showEnableLocationToast()
        }

        rxBleClient.observeStateChanges()
            .subscribe {
                when(it){
                    RxBleClient.State.LOCATION_SERVICES_NOT_ENABLED -> {
                            showEnableLocationToast()
                            scanDisposable.dispose()
                    }
                    RxBleClient.State.READY -> {
                        scanDisposable = scanLeDevice()
                    }
                    else -> {
                        Timber.d("Problem with: ${it.name}")
                    }
                }
            }.addTo(compositeDisposable)
    }

    private fun showEnableLocationToast() {
        Toast.makeText(
            this,
            "Please enable Location to use ${getString(R.string.app_name)}",
            Toast.LENGTH_LONG
        ).show()
    }
}