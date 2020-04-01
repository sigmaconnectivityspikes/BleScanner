package se.sigmaconnectivity.blescanner.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import se.sigmaconnectivity.blescanner.domain.feature.FeatureStatus
import se.sigmaconnectivity.blescanner.domain.usecase.ContactUseCase
import se.sigmaconnectivity.blescanner.ui.base.BaseViewModel

class HomeViewModel(private val contactUseCase: ContactUseCase) : BaseViewModel() {
    private val mutableLEServiceStatus = MutableLiveData<FeatureStatus>()
    val leServiceStatusEvent: LiveData<FeatureStatus> = mutableLEServiceStatus
    val devicesAmount = MutableLiveData<String>()
    //TODO implement last update time
    val lastUpdateHour = MutableLiveData<String>().apply { value = "14:55" }
    val lastUpdateDate = MutableLiveData<String>().apply { value = "Mar 20" }
    private val error = MutableLiveData<String>()
    val errorEvent: LiveData<String> = error

    init {
        disposables.add(
            //TODO add get today scanned devices count
            contactUseCase.getDevicesCount()
                .subscribe({ count ->
                    devicesAmount.value = count.toString()
                }, {
                    error.value = it.message
                })
        )
    }

    fun turnOnLEService() {
        mutableLEServiceStatus.value = FeatureStatus.ACTIVE
    }

    fun turnOffLEService() {
        mutableLEServiceStatus.value = FeatureStatus.INACTIVE
    }
}