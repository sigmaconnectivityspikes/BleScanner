package se.sigmaconnectivity.blescanner.ui.help

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_help.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import se.sigmaconnectivity.blescanner.R
import se.sigmaconnectivity.blescanner.databinding.FragmentHelpBinding
import se.sigmaconnectivity.blescanner.ui.common.livedata.observe


class HelpFragment : Fragment() {

    private val vm: HelpViewModel by viewModel()
    private lateinit var binding: FragmentHelpBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_help,
            container,
            false
        )
        binding.vm = vm
        binding.lifecycleOwner = this

        initView()

        return binding.root
    }

    private fun initView() {
        vm.userId.observe(this, ::updateUserId)
    }

    private fun updateUserId(bitmap: Bitmap) {
        ivQrCode.setImageBitmap(replaceWhiteWithTransparent(bitmap))
    }

    //TODO move to use case
    private fun replaceWhiteWithTransparent(source: Bitmap): Bitmap {
        val target = Bitmap.createBitmap(source)
        context?.let {
            val whiteColor = ContextCompat.getColor(it, android.R.color.white)
            val transparentColor = ContextCompat.getColor(it, android.R.color.transparent)
            (0 until source.width).forEach { x ->
                (0 until source.height).forEach { y ->
                    if (source.getPixel(x, y) == whiteColor) {
                        target.setPixel(x, y, transparentColor)
                    }
                }
            }
        }
        return target
    }
}
