package se.sigmaconnectivity.blescanner.domain.model

enum class OutgoingBridgeDataType(val code: Int) {
    DUMMY_DATA(0);
    companion object {
        fun valueOf(value: Int): OutgoingBridgeDataType = values().find { it.code == value } ?: throw IllegalAccessException()
    }
}