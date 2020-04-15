package se.sigmaconnectivity.blescanner.ui.home

import se.sigmaconnectivity.blescanner.ui.common.BaseViewModel

class HomeViewModel() : BaseViewModel() {

    fun setBridgeData(dataType: Int, dataJson: String) {
       //DUMMY
    }

    fun getBridgeData(dataType: Int): String {
        return "DUMMY"/*Gson().toJson( //TODO gson model
            contactUseCase.getContacts()
                .blockingFirst()
        )*/
    }
}
