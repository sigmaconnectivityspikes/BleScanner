package se.sigmaconnectivity.blescanner.ui.home

import android.webkit.JavascriptInterface


class NativeBridgeInterface(val vm: HomeViewModel) {

    @JavascriptInterface
    fun setPhoneNumberHash(hash: String) {
        vm.setPhoneNumberHash(hash)
    }

    @JavascriptInterface
    fun getMatchedDevices() : String {
        return "{}" //TODO pass json with proper model
    }

    companion object {
        const val NATIVE_BRIDGE_NAME = "NativeBridge"
    }
}