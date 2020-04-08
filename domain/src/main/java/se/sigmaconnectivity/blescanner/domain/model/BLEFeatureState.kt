package se.sigmaconnectivity.blescanner.domain.model

sealed class BLEFeatureState {
    object Started: BLEFeatureState()
    object Stopped: BLEFeatureState()

    class Error(val type: StatusErrorType): BLEFeatureState()
}