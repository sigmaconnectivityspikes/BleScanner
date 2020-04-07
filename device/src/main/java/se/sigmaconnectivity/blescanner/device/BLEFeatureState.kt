package se.sigmaconnectivity.blescanner.device

sealed class BLEFeatureState {
    object Started: BLEFeatureState()
    object Stopped: BLEFeatureState()

    class Error(val type: StatusErrorType): BLEFeatureState()
}