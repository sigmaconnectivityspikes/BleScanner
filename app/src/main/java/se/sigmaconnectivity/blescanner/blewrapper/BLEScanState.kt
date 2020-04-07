package se.sigmaconnectivity.blescanner.blewrapper

sealed class BLEScanState {
    object Ready: BLEScanState()
    object Started: BLEScanState()
    object Stopped: BLEScanState()

    class Error(val type: StatusErrorType): BLEScanState()
}