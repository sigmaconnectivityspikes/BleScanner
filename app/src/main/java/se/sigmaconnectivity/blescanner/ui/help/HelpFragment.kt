package se.sigmaconnectivity.blescanner.ui.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.fragment_help.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import se.sigmaconnectivity.blescanner.R
import se.sigmaconnectivity.blescanner.livedata.observe

class HelpFragment : Fragment() {

    private val vm: HelpViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        initView()
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_help, container, false)
    }

    private fun initView() {
        vm.userId.observe(this, ::updateUserId)
    }

    private fun updateUserId(userId: String) {
        val barcodeEncoder = BarcodeEncoder()
        val bitmap = barcodeEncoder.encodeBitmap(userId, BarcodeFormat.QR_CODE, 500, 500)
        ivQrCode.setImageBitmap(bitmap)
        tvQrContent.text = userId
    }

}
