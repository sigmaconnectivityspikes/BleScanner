package se.sigmaconnectivity.blescanner.ui.home

import se.sigmaconnectivity.blescanner.ui.common.BaseViewModel

class HomeViewModel : BaseViewModel() {
    fun setPhoneNumberHash(hash: String) {
       //DUMMY
    }

    fun getDeviceMetrics(): String {
        return "DUMMY"/*Gson().toJson( //TODO gson model
            contactUseCase.getContacts()
                .blockingFirst()
        )*/
    }
}
