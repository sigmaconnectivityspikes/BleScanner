package se.sigmaconnectivity.blescanner.ui.help

import androidx.lifecycle.ViewModel
import timber.log.Timber

class HelpViewModel() :
    ViewModel() {

    fun onHospitalsSelected() {
        //TODO impl
        Timber.d("Hospitals selected")
    }

    fun onPharmaciesSelected() {
        //TODO impl
        Timber.d("Pharmacies selected")
    }

    fun onSuppliesSelected() {
        //TODO impl
        Timber.d("Supplies selected")
    }

    fun onUsefulInfoSelected() {
        //TODO impl
        Timber.d("UsefulInfo selected")
    }
}