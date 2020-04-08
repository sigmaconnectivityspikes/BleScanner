package se.sigmaconnectivity.blescanner.device

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import se.sigmaconnectivity.blescanner.domain.LocationStatusRepository
import se.sigmaconnectivity.blescanner.domain.model.LocationStatus
import timber.log.Timber

class LocationStatusRepositoryImpl(private val context: Context) : LocationStatusRepository {
    private val locationStateSubject: BehaviorSubject<LocationStatus> = BehaviorSubject.create()

    private val locationProviderReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.action?.let { action ->
                if(action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                    Timber.d("Location state change received")
                    verifyLocationEnabled()
                }
            }
        }
    }

    init {
        verifyLocationEnabled()
        registerLocationProviderReceiver()
    }

    private fun verifyLocationEnabled() {
        val locationEnabled = isLocationEnabled()
        locationStateSubject.onNext(
            if (locationEnabled) {
                LocationStatus.READY
            } else {
                LocationStatus.NOT_READY
            }
        )
    }

    private fun isLocationEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.isLocationEnabled
        } else {
            @Suppress("DEPRECATION")
            Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.LOCATION_MODE
            ) != Settings.Secure.LOCATION_MODE_OFF
        }
    }

    private fun registerLocationProviderReceiver() {
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        context.registerReceiver(locationProviderReceiver, filter)
    }

    override val trackLocationStatus: Observable<LocationStatus>
        get() = locationStateSubject.hide()
}