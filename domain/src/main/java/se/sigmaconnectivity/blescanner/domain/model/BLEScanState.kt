package se.sigmaconnectivity.blescanner.domain.model

sealed class BLEScanState {
    object Started: BLEScanState()
    object Stopped: BLEScanState()

    class Error(val type: StatusErrorType): BLEScanState()
}