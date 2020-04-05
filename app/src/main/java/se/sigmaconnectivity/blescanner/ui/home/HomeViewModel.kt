package se.sigmaconnectivity.blescanner.ui.home

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.rxkotlin.addTo
import se.sigmaconnectivity.blescanner.domain.usecase.ContactUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.UserUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.device.SubscribeForBluetoothStatusUseCase
import se.sigmaconnectivity.blescanner.extensions.isBluetoothEnabled
import se.sigmaconnectivity.blescanner.service.BleScanService
import se.sigmaconnectivity.blescanner.ui.common.BaseViewModel
import timber.log.Timber

class HomeViewModel(
    private val contactUseCase: ContactUseCase,
    private val userUseCase: UserUseCase,
    private val appContext: Context
) : BaseViewModel() {

    private val serviceIntent: Intent by lazy {
        Intent(appContext, BleScanService::class.java)
    }

    private val devicesAmount = MutableLiveData<String>()

    //TODO implement last update time
    private val lastUpdateHour = MutableLiveData<String>().apply { value = "14:55" }
    private val lastUpdateDate = MutableLiveData<String>().apply { value = "Mar 20" }
    private val error = MutableLiveData<ErrorEvent>()

    val errorEvent: LiveData<ErrorEvent> = error

    init {
        //TODO add get today scanned devices count
        contactUseCase.getDevicesCount()
            .subscribe({ count ->
                devicesAmount.value = count.toString()
            }, {
                error.value = ErrorEvent.Unknown(it.message ?: "Error")
            }).addTo(disposables)
        userUseCase.getUserHash()
            .subscribe({ if (it.isNotEmpty()) turnOnBleService() }, {
                error.value = ErrorEvent.Unknown(it.message ?: "Error")
            }).addTo(disposables)
    }

    fun setPhoneNumberHash(hash: String) {
        userUseCase.saveUserHash(hash)
            .subscribe({ onUserUpdated() }, {
                error.value = ErrorEvent.Unknown(it.message ?: "Error")
            }).addTo(disposables)
    }

    private fun onUserUpdated() {
        turnOnBleService()
    }

    fun getDeviceMetrics(): String {
        return "TODO"
    }

    fun toggleBleService() {
        if (BleScanService.isRunning.get()) {
            turnOffBleService()
        } else {
            turnOnBleService()
        }
    }

    private fun turnOnBleService() {
        Timber.d("Turning on BLE scan")
        if (appContext.isBluetoothEnabled()) {
            ContextCompat.startForegroundService(appContext, serviceIntent)
        } else {
            error.value = ErrorEvent.BluetoothNotEnabled
        }
    }

    private fun turnOffBleService() {
        Timber.d("Turning off BLE scan")
        appContext.stopService(serviceIntent)
    }

    sealed class ErrorEvent {
        object BluetoothNotEnabled : ErrorEvent()
        data class Unknown(val message: String) : ErrorEvent()
    }
}
