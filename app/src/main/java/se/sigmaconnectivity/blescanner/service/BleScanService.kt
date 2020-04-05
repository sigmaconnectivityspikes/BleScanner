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
import io.reactivex.rxkotlin.addTo
import org.koin.android.ext.android.inject
import org.threeten.bp.Duration
import se.sigmaconnectivity.blescanner.Consts
import se.sigmaconnectivity.blescanner.R
import se.sigmaconnectivity.blescanner.data.HASH_SIZE_BYTES
import se.sigmaconnectivity.blescanner.data.isValidChecksum
import se.sigmaconnectivity.blescanner.data.toChecksum
import se.sigmaconnectivity.blescanner.data.toHash
import se.sigmaconnectivity.blescanner.domain.HashConverter
import se.sigmaconnectivity.blescanner.domain.feature.FeatureStatus
import se.sigmaconnectivity.blescanner.domain.usecase.ContactUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.GetUserIdHashUseCase
import se.sigmaconnectivity.blescanner.ui.MainActivity
import timber.log.Timber
import java.nio.ByteBuffer
import java.util.*

class BleScanService() : Service() {

    private val rxBleClient: RxBleClient by inject()
    private val contactUseCase: ContactUseCase by inject()
    private val getUserIdHashUseCase: GetUserIdHashUseCase by inject()

    //TODO: Use usecase for this conversion
    private val hashConverter: HashConverter by inject()
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
            if (errorCode == ADVERTISE_FAILED_ALREADY_STARTED) {
                advertiseStatus = FeatureStatus.ACTIVE
                updateNotification()
            } else {
                advertiseStatus = FeatureStatus.INACTIVE
                updateNotification()
            }
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

        getUserIdHashUseCase.execute().subscribe({ userUid ->
            val serviceUUID =  UUID.fromString(Consts.SERVICE_UUID)
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

    private fun scanLeDevice() {
        val scanSettingsAll = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .build()
        val scanSettingsLost = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_MATCH_LOST)
            .build()
        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid.fromString(Consts.SERVICE_UUID))
            .build()
        rxBleClient.scanBleDevices(scanSettingsAll, scanFilter)
            .mergeWith(rxBleClient.scanBleDevices(scanSettingsLost, scanFilter))
            .doOnSubscribe {
                Timber.d("scanLeDevice started")
                scanStatus = FeatureStatus.ACTIVE
            }
            .doOnDispose {
                Timber.d("scanLeDevice disposed")
                scanStatus = FeatureStatus.INACTIVE
            }
            .subscribe(
                { scanResult ->
                    val timestampMillis = System.currentTimeMillis() //TODO time from scanResult
                    Timber.d("-BT- found scanResult ${scanResult.scanRecord.serviceUuids} callbackType ${scanResult.callbackType}")
                    assembleUID(scanResult)?.let {
                        if (scanResult.callbackType == ScanCallbackType.CALLBACK_TYPE_ALL_MATCHES) {
                            processMatch(it, timestampMillis)
                        } else if (scanResult.callbackType == ScanCallbackType.CALLBACK_TYPE_MATCH_LOST) {
                            processMatchLost(it, timestampMillis)
                        }
                    } ?: Timber.e("Can not assemble UID")
                },
                {
                    Timber.d(it, "Device found with error")
                }
            ).addTo(compositeDisposable)
    }

    private val existingMatchingHashes = HashSet<String>()

    private fun processMatch(contactHash: String, timestamp: Long) {
        Timber.d("-BT- checking match: $contactHash in $existingMatchingHashes")
        if (!existingMatchingHashes.contains(contactHash)) {
            Timber.d("-BT- found match: $contactHash")
            existingMatchingHashes.add(contactHash)
            Timber.d("CALLBACK_TYPE_FIRST_MATCH: $contactHash")
            contactUseCase.processContactMatch(contactHash, timestamp)
                .subscribe({
                    Timber.d("processContactMatch() SUCCESS")
                }, {
                    Timber.e(it, "processContactMatch() FAILED")
                }).addTo(compositeDisposable)
        }
    }

    private fun processMatchLost(contactHash: String, timestamp: Long) {
        Timber.d("CALLBACK_TYPE_MATCH_LOST: $contactHash")
        existingMatchingHashes.remove(contactHash)
        contactUseCase.processContactLost(contactHash, timestamp)
            .subscribe({
                Timber.d("processContactLost() SUCCESS")
            }, {
                Timber.d("processContactLost() FAILED \n $it")
            }).addTo(compositeDisposable)
    }

    private fun assembleUID(scanResult: ScanResult): String? {
        val results = scanResult.scanRecord.getManufacturerSpecificData(Consts.MANUFACTURER_ID)
        Timber.d("BT- scan result uuid ${scanResult.scanRecord.serviceUuids}")
        return results?.let {
            //TODO: change it to chained rx invocation
            val bytes = ByteBuffer.allocate(8)
                .put(it)
            val hashBytes = bytes.array().sliceArray(0 until HASH_SIZE_BYTES )
            val checksum = bytes.array()[HASH_SIZE_BYTES]
            if (hashBytes.isValidChecksum(checksum)) {
                val data = hashConverter.convert(hashBytes).blockingGet()
                Timber.d("BT- data received: $data")
                data
            } else {
                null
            }
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