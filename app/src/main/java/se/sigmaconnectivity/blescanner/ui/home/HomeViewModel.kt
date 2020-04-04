package se.sigmaconnectivity.blescanner.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.rxkotlin.addTo
import se.sigmaconnectivity.blescanner.domain.feature.FeatureStatus
import se.sigmaconnectivity.blescanner.domain.usecase.ContactUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.UserUseCase
import se.sigmaconnectivity.blescanner.ui.common.BaseViewModel

class HomeViewModel(
    private val contactUseCase: ContactUseCase,
    private val userUseCase: UserUseCase
) : BaseViewModel() {
    private val mutableLEServiceStatus = MutableLiveData<FeatureStatus>()
    val leServiceStatusEvent: LiveData<FeatureStatus> = mutableLEServiceStatus
    val devicesAmount = MutableLiveData<String>()

    //TODO implement last update time
    val lastUpdateHour = MutableLiveData<String>().apply { value = "14:55" }
    val lastUpdateDate = MutableLiveData<String>().apply { value = "Mar 20" }
    private val error = MutableLiveData<String>()
    val errorEvent: LiveData<String> = error

    init {
        //TODO add get today scanned devices count
        contactUseCase.getDevicesCount()
            .subscribe({ count ->
                devicesAmount.value = count.toString()
            }, {
                error.value = it.message
            }).addTo(disposables)
    }

    fun setPhoneNumberHash(hash: String) {
        userUseCase.saveUserHash(hash)
            .subscribe({ onUserUpdated() }, {
                error.value = it.message
            }).addTo(disposables)
    }

    private fun onUserUpdated() {
        turnOnLEService()
    }

    fun getDeviceMetrics() : String {
        return "TODO"
    }

    fun toggleLEService() {
        if (mutableLEServiceStatus.value == FeatureStatus.ACTIVE)
            turnOffLEService()
        else
            turnOnLEService()
    }

    private fun turnOnLEService() {
        mutableLEServiceStatus.value = FeatureStatus.ACTIVE
    }

    private fun turnOffLEService() {
        mutableLEServiceStatus.value = FeatureStatus.INACTIVE
    }
}
