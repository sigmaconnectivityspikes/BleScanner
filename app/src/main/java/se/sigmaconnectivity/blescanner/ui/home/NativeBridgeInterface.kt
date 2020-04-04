package se.sigmaconnectivity.blescanner.ui.home

import android.webkit.JavascriptInterface
import timber.log.Timber


class NativeBridgeInterface(
    private val setPhoneNumberHashCallback: (hash: String) -> Unit,
    private val getMatchedDevicesCallback: () -> String
) {

    @JavascriptInterface
    fun setPhoneNumberHash(hash: String) {
        Timber.d("setPhoneNumberHash: $hash")
        setPhoneNumberHashCallback(hash)
    }

    @JavascriptInterface
    fun getMatchedDevices(): String {
        Timber.d("getMatchedDevices called")
        return getMatchedDevicesCallback()
    }

    companion object {
        const val NATIVE_BRIDGE_NAME = "NativeBridge"
    }
}