package com.wolt.restaurant.utils

object Constants {
    internal const val POST_URI = "/openingHours"
    internal const val EXP_MSG_UNEXP_OPENING = "Unexpected opening time!"
    internal const val EXP_MSG_UNEXP_CLOSING = "Unexpected closing time!"
    internal const val EXP_MSG_NON_SEQUENTIAL = "Entered days in working times info are not sequential!"
    internal const val EXP_MSG_INACCURATE_TIMING = "Opening time and closing time incompatible!"
    internal const val ALL_DAY_CLOSED = "Closed"
    internal const val DEFAULT_TIMEZONE = "GMT"
    internal const val HOUR_FORMAT_12 = "hh:mm a"
}
