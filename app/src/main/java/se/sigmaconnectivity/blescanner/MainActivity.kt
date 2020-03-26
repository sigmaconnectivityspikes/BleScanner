package se.sigmaconnectivity.blescanner

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private val serviceIntent: Intent by lazy {
        Intent(this, BleScanService::class.java)
    }
    private val rxPermissions by lazy {
        RxPermissions(this)
    }

    private val compositeDispose = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (hasBLE()) {
            requestPermissions()
        } else {
            Toast.makeText(this, "Device does not support BLE", Toast.LENGTH_LONG).show()
        }

        initView()
    }

    private fun hasBLE(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE).also {
            if (!it) Toast.makeText(this, "BLE not supported", Toast.LENGTH_LONG).show()
        }
    }

    private fun initView() {
        btn_startService.setOnClickListener {
            createNotificationChannel()
            ContextCompat.startForegroundService(this, serviceIntent)
        }
        btn_stopService.setOnClickListener { stopService(serviceIntent) }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestBackgroundPerm()
        } else {
            requestLocationPerm()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundPerm() {
        compositeDispose.add(
            rxPermissions.request(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
                .subscribe {
                    if (!it) {
                        Toast.makeText(this, "Location access is required", Toast.LENGTH_LONG)
                            .show()
                        requestLocationPerm()
                    }
                }
        )
    }

    private fun requestLocationPerm() {
        compositeDispose.add(
            rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe {
                    if (!it) {
                        Toast.makeText(this, "Location access is required", Toast.LENGTH_LONG)
                            .show()
                        requestLocationPerm()
                    }
                }
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                Consts.NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                enableVibration(false)
                enableLights(false)
            }
            (getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager?)?.let {
                it.createNotificationChannel(serviceChannel)
                Timber.d("createNotificationChannel: ${serviceChannel.id}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDispose.clear()
    }
}
