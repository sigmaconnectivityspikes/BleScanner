package se.sigmaconnectivity.blescanner.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import se.sigmaconnectivity.blescanner.ui.common.BaseViewModel

class HomeViewModel() : BaseViewModel() {

    private val devicesAmount = MutableLiveData<String>()

    //TODO implement last update time
    private val lastUpdateHour = MutableLiveData<String>().apply { value = "14:55" }
    private val lastUpdateDate = MutableLiveData<String>().apply { value = "Mar 20" }
    private val error = MutableLiveData<ErrorEvent>()

    val errorEvent: LiveData<ErrorEvent> = error

    fun setPhoneNumberHash(hash: String) {
       //DUMMY
    }


    fun getDeviceMetrics(): String {
        return "DUMMY"/*Gson().toJson( //TODO gson model
            contactUseCase.getContacts()
                .blockingFirst()
        )*/
    }

    sealed class ErrorEvent {
        object BluetoothNotEnabled : ErrorEvent()
        data class Unknown(val message: String) : ErrorEvent()
    }
}
