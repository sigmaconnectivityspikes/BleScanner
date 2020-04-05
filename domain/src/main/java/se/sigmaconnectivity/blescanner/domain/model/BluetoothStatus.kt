package se.sigmaconnectivity.blescanner.domain.model

sealed class BluetoothStatus {
    object TurnedOn: BluetoothStatus()
    object TurnedOff: BluetoothStatus()
}