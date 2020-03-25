package se.sigmaconnectivity.blescanner

import android.Manifest
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

        if (deviceHasBLE()) requestPermissions()

        btn_startService.setOnClickListener {
            ContextCompat.startForegroundService(
                this,
                serviceIntent
            )
        }
        btn_stopService.setOnClickListener { stopService(serviceIntent) }
    }

    private fun deviceHasBLE(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE).also {
            if (!it) Toast.makeText(this, "BLE not supported", Toast.LENGTH_LONG).show()
        }
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
            rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                .subscribe {
                    requestBT()
                }
        )
    }

    private fun requestLocationPerm() {
        compositeDispose.add(
            rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe {
                    requestBT()
                }
        )
    }

    private fun requestBT() {
        compositeDispose.add(
            rxPermissions.request(Manifest.permission.BLUETOOTH_ADMIN)
                .subscribe()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDispose.clear()
    }
}
