package se.sigmaconnectivity.blescanner.device

import kotlin.math.pow

class DistanceCalculator {
    fun calculate(rssi: Int, txPower: Int): Double {
        val result = 10.0.pow((txPower.toDouble() - rssi) / (10 * 2))
        println("-DIST-- Distance calculation:  $rssi $txPower calc $result")
        return result
    }
}