package se.sigmaconnectivity.blescanner.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import se.sigmaconnectivity.blescanner.domain.feature.FeatureStatus
import se.sigmaconnectivity.blescanner.ui.base.BaseViewModel

class HomeViewModel : BaseViewModel() {
    private val mutableLEServiceStatus = MutableLiveData<FeatureStatus>()
    val leServiceStatusEvent: LiveData<FeatureStatus> = mutableLEServiceStatus

    fun turnOnLEService() {
        mutableLEServiceStatus.value = FeatureStatus.ACTIVE
    }

    fun turnOffLEService() {
        mutableLEServiceStatus.value = FeatureStatus.INACTIVE
    }
}