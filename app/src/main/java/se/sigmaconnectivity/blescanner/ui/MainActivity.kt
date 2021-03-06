package se.sigmaconnectivity.blescanner.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import se.sigmaconnectivity.blescanner.Consts
import se.sigmaconnectivity.blescanner.MainGraphDirections
import se.sigmaconnectivity.blescanner.R
import se.sigmaconnectivity.blescanner.databinding.ActivityMainBinding
import se.sigmaconnectivity.blescanner.domain.usecase.TrackInfectionsUseCase
import se.sigmaconnectivity.blescanner.ui.common.livedata.observe
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    companion object {
        fun getIntent(context: Context) = Intent(context, MainActivity::class.java)
    }

    private val rxPermissions by lazy {
        RxPermissions(this)
    }
    private val trackInfectionsUseCase: TrackInfectionsUseCase by inject()

    private val mainViewModel by viewModel<MainViewModel>()

    private val compositeDispose = CompositeDisposable()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        setUpNavigation()

        if (hasBLE()) {
            requestPermissions()
        }

        createNotificationChannel()
        initializeFcm()
    }

    private fun hasBLE(): Boolean {
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
            }.addTo(compositeDispose)
    }

    private fun requestLocationPerm() {
        rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION)
            .subscribe {
                if (!it) {
                    Toast.makeText(this, "Location access is required", Toast.LENGTH_LONG)
                        .show()
                    requestLocationPerm()
                }
            }.addTo(compositeDispose)
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

    private fun initializeFcm() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Timber.w(task.exception, "Couldn't get FCM token")
                    return@OnCompleteListener
                }

                val token = task.result?.token
                // Log and toast
                Timber.d("FCM token $token")
            })

        FirebaseMessaging.getInstance().subscribeToTopic("infections")
            .addOnCompleteListener { task ->
                var msg = "FCM topic subscribe success"
                if (!task.isSuccessful) {
                    msg = "FCM topic subscribe failed"
                }
                Timber.d(msg)
            }
        trackInfectionsUseCase.execute().subscribe({
            val message = "New infection: $it"
            Timber.d(message)
            Toast.makeText(baseContext, message, Toast.LENGTH_LONG).show()
        },
            {
                Timber.e(it)
            }).addTo(compositeDispose)

    }

    private fun setUpNavigation() {
        //TODO change infection UX
        /*val navHostFragment: NavHostFragment? = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as? NavHostFragment
        if (navHostFragment != null) {
            NavigationUI.set(
                binding.bottomNavigation,
                navHostFragment.navController
            )
        }*/
        mainViewModel.showInfectionMessage.observe(this, ::navigateToInfectionMessage)
    }

    private fun navigateToInfectionMessage() {
        findNavController(R.id.navHostFragment).navigate(MainGraphDirections.actionToInfoDialog(R.string.possible_infection_info))
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDispose.clear()
    }
}
