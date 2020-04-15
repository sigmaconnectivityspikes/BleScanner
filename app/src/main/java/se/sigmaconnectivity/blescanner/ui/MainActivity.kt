package se.sigmaconnectivity.blescanner.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.CompositeDisposable
import org.koin.androidx.viewmodel.ext.android.viewModel
import se.sigmaconnectivity.blescanner.Consts
import se.sigmaconnectivity.blescanner.R
import se.sigmaconnectivity.blescanner.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    companion object {
        fun getIntent(context: Context) = Intent(context, MainActivity::class.java)
    }

    private val rxPermissions by lazy {
        RxPermissions(this)
    }

    private val mainViewModel by viewModel<MainViewModel>()

    private val compositeDispose = CompositeDisposable()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        setUpNavigation()

        createNotificationChannel()
        initializeFcm()
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
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDispose.clear()
    }
}
