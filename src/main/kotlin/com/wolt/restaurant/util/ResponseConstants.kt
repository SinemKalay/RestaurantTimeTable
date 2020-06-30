package com.wolt.restaurant.util

enum class ResponseConstants(val value: String) {
    SUCCESS("success"),
    NO_SUCH_DAY("BAD_DATA_0001"),
    NO_SUCH_TYPE("BAD_DATA_0002"),
    CORRUPTED_DATA("CORRPTD_DATA"),
    INCOMP_DATA("Incompatible_DATA")
}