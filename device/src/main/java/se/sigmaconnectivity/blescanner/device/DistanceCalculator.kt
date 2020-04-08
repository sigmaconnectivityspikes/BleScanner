package se.sigmaconnectivity.blescanner.device

import kotlin.math.pow

class DistanceCalculator {
    fun calculate(rssi: Int, txPower: Int): Double {
        println("Contact items-  $rssi $txPower calc ${10.0.pow((txPower.toDouble() - rssi) / (10 * 2))}")
        return 10.0.pow((txPower.toDouble() - rssi) / (10 * 2))
    }
}