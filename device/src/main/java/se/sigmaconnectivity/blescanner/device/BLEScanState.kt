package se.sigmaconnectivity.blescanner.device

sealed class BLEScanState {
    object Started: BLEScanState()
    object Stopped: BLEScanState()

    class Error(val type: StatusErrorType): BLEScanState()
}