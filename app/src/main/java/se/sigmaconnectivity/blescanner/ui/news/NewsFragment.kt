package se.sigmaconnectivity.blescanner.ui.news

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import se.sigmaconnectivity.blescanner.R
import se.sigmaconnectivity.blescanner.databinding.FragmentNewsBinding

class NewsFragment : Fragment() {

    private lateinit var binding: FragmentNewsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_news,
            container,
            false
        )
        binding.lifecycleOwner = this

        initWebView()

        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        val url = getString(R.string.news_url)
        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = WebViewClient()
            loadUrl(url)
        }
    }

}
