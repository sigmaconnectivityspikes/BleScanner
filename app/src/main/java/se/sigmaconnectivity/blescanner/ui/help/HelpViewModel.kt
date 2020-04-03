package se.sigmaconnectivity.blescanner.ui.help

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.ViewModel
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import se.sigmaconnectivity.blescanner.domain.usecase.GetHumanReadableUserIdUseCase
import timber.log.Timber

class HelpViewModel(private val getHumanReadableUserIdUseCase: GetHumanReadableUserIdUseCase) :
    ViewModel() {

    val userId: LiveData<Bitmap> =
        LiveDataReactiveStreams.fromPublisher(
            getHumanReadableUserIdUseCase.execute()
                .map { userId: String ->
                    BarcodeEncoder().encodeBitmap(userId, BarcodeFormat.QR_CODE, 500, 500)
                }.toFlowable()
        )

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