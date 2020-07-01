package com.wolt.restaurant.util

object Constants {
    internal const val POST_URI = "/convertTimesToReadableFormat"
    internal const val EXP_MSG_WAIT_CLOSING = "Unclosed day exists!"
    internal const val EXP_MSG_UNEXP_OPENING = "Unexpected opening time!"
    internal const val EXP_MSG_UNEXP_CLOSING = "Unexpected closing time!"
    internal const val EXP_MSG_NON_SEQUENTIAL = "Entered days in working times info are not sequential!"
    internal const val EXP_MSG_INACCURATE_TIMING = "Corrupted time value!"
    internal const val EXP_MSG_TYPE_NOT_FOUND = "Type value not found!"
    internal const val REASON_TYPE_NOT_FOUND = "Type field is required"
    internal const val EXP_MSG_VALUE_NOT_FOUND = "Time value not found!"
    internal const val REASON_VALUE_NOT_FOUND = "Value field is required"
    internal const val REASON_INACCURATE_TIMING_VALUE = "Value field should be exist and in between 0 and 86400."
    internal const val EXP_MSG_NO_SUCH_DAY = "No such day found."
    internal const val REASON_NO_SUCH_DAY = "Day names must be one of the followings:"
    internal const val EXP_MSG_NO_TYPE_DAY = "No such type found."
    internal const val REASON_NO_TYPE_DAY = "Type names must be one of the followings:"
    internal const val ALL_DAY_CLOSED = "Closed"
    internal const val DEFAULT_TIMEZONE = "GMT"
    internal const val HOUR_FORMAT_12 = "hh:mm a"
}
