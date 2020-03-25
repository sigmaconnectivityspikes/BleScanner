package se.sigmaconnectivity.blescanner

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.disposables.CompositeDisposable
import org.koin.android.ext.android.inject


class BleScanService() : Service() {

    private val rxBleClient: RxBleClient by inject()
    private val compositeDisposable = CompositeDisposable()
    private val TAG = this::class.simpleName

    override fun onBind(intent: Intent): IBinder {
        return Binder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        createNotificationChannel()
        startForeground(1, createNotification())
        startScan()

        return START_NOT_STICKY
    }

    private fun startScan() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        bluetoothManager?.adapter?.let {
            scanLeDevice()
        } ?: Log.e(TAG, "BT not supported")
    }

    private fun scanLeDevice() {
        val scanSettings =
            ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()
        println("scan devices started")
        compositeDisposable.add(
            rxBleClient.scanBleDevices(scanSettings, ScanFilter.Builder().setDeviceName(null).build())
                .subscribe(
                    {
                        println("Device finded")
                        println(it.bleDevice.toString())
                        Log.d(TAG, "Device finded ${it.bleDevice.bluetoothDevice.address}")
                    },
                    {
                        println("Device errored")
                        println(it)
                    }
                )
        )
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        stopForeground(true)
        stopSelf()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service")
            .setContentIntent(pendingIntent)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "ForegroundServiceChannel"
    }
}
