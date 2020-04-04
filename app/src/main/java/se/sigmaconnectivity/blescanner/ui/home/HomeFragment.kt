package se.sigmaconnectivity.blescanner.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import se.sigmaconnectivity.blescanner.BuildConfig
import se.sigmaconnectivity.blescanner.R
import se.sigmaconnectivity.blescanner.databinding.FragmentHomeBinding
import se.sigmaconnectivity.blescanner.service.BleScanService
import se.sigmaconnectivity.blescanner.ui.common.BaseFragment

class HomeFragment : BaseFragment() {

    private val vm: HomeViewModel by viewModel()
    private lateinit var binding: FragmentHomeBinding

    private val serviceIntent: Intent by lazy {
        Intent(context, BleScanService::class.java)
    }

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

        registerObservers()
        setUpWebView()

        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebView() {
        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = WebViewClient()
            addJavascriptInterface(NativeBridgeInterface(vm), NativeBridgeInterface.NATIVE_BRIDGE_NAME)
            loadUrl(BuildConfig.WEB)
        }
    }

    private fun registerObservers() {
        vm.leServiceStatusEvent.observe(viewLifecycleOwner, Observer { featureStatus ->
            /*if (featureStatus == FeatureStatus.ACTIVE) {
                context?.let { ContextCompat.startForegroundService(it, serviceIntent) }
            } else {
                binding.btnStopService.setOnClickListener { context?.stopService(serviceIntent) }
            }*/
        })
        vm.errorEvent.observe(viewLifecycleOwner, Observer { errorMessage ->
            view?.let { Snackbar.make(it, errorMessage, Snackbar.LENGTH_SHORT) }
        })
    }

}
