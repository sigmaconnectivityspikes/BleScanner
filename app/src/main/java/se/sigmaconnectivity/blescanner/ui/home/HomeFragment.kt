package se.sigmaconnectivity.blescanner.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import org.koin.androidx.viewmodel.ext.android.viewModel
import se.sigmaconnectivity.blescanner.BuildConfig
import se.sigmaconnectivity.blescanner.Consts
import se.sigmaconnectivity.blescanner.R
import se.sigmaconnectivity.blescanner.databinding.FragmentHomeBinding
import se.sigmaconnectivity.blescanner.ui.common.BaseFragment
import timber.log.Timber


class HomeFragment : BaseFragment() {

    private val vm: HomeViewModel by viewModel()
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home,
            container,
            false
        )
        binding.vm = vm
        binding.lifecycleOwner = this

        activity?.intent?.let {
            if (!it.getStringExtra(Consts.NOTIFICATION_EXTRA_DATA).isNullOrBlank()) {
                //TODO impl
                Timber.d("Data from notification received: ${it.getStringExtra(Consts.NOTIFICATION_EXTRA_DATA)}")
            }
        }

        setUpWebView()

        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebView() {
        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = WebViewClient()
            addJavascriptInterface(
                NativeBridgeInterface(
                    vm::setPhoneNumberHash,
                    vm::getDeviceMetrics
                ), NativeBridgeInterface.NATIVE_BRIDGE_NAME
            )
            loadUrl(BuildConfig.WEB)
        }
        binding.webView.setOnLongClickListener {
            false
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.webView.canGoBack()) {
                        binding.webView.goBack()
                    } else {
                        activity?.finish() //TODO handle back normally by activity
                    }
                }
            })
    }
}
