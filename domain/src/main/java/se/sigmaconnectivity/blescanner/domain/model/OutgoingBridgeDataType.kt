package se.sigmaconnectivity.blescanner.domain.model

enum class OutgoingBridgeDataType(val code: Int) {
    DUMMY_DATA(0),
    NOTIFICATION_DATA(2);
    companion object {
        fun valueOf(value: Int): OutgoingBridgeDataType = values().find { it.code == value } ?: throw IllegalAccessException()
    }
}