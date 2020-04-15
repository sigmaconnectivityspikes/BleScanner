package se.sigmaconnectivity.blescanner.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import se.sigmaconnectivity.blescanner.ui.common.BaseViewModel

class HomeViewModel() : BaseViewModel() {

    private val error = MutableLiveData<ErrorEvent>()

    val errorEvent: LiveData<ErrorEvent> = error

    fun setBridgeData(dataType: Int, dataJson: String) {
       //DUMMY
    }

    fun getBridgeData(dataType: Int): String {
        return "DUMMY"/*Gson().toJson( //TODO gson model
            contactUseCase.getContacts()
                .blockingFirst()
        )*/
    }

    sealed class ErrorEvent {
        data class Unknown(val message: String) : ErrorEvent()
    }
}
