package com.wolt.restaurant.util

object Constants {
    const val POST_URI = "/convertTimesToReadableFormat"
    const val MAX_TIME_VALUE = 86399
    const val MIN_TIME_VALUE = 0
    const val EXP_MSG_WAIT_CLOSING = "Unclosed day exists!"
    const val EXP_MSG_UNEXP_OPENING = "Unexpected opening time!"
    const val EXP_MSG_UNEXP_CLOSING = "Unexpected closing time!"
    const val EXP_MSG_NON_SEQUENTIAL = "Entered days in working times info are not sequential!"
    const val EXP_MSG_INACCURATE_TIMING = "Corrupted time value!"
    const val EXP_MSG_RESP_BODY_NOT_FOUND = "Required request body is missing!"
    const val REASON_RESP_BODY_NOT_FOUND = "Appropriate response body should be provided"
    const val EXP_MSG_TYPE_NOT_FOUND = "Type value not found!"
    const val REASON_TYPE_NOT_FOUND = "Type field is required"
    const val EXP_MSG_VALUE_NOT_FOUND = "Time value not found!"
    const val REASON_VALUE_NOT_FOUND = "Value field is required"
    const val REASON_INACCURATE_TIMING_VALUE = "Value field should be exist and in between 0 and 86400."
    const val EXP_MSG_NO_SUCH_DAY = "No such day found."
    const val REASON_NO_SUCH_DAY = "Day names must be one of the followings:"
    const val EXP_MSG_NO_SUCH_TYPE = "No such type found."
    const val REASON_NO_SUCH_TYPE = "Type names must be one of the followings:"
    const val ALL_DAY_CLOSED = "Closed"
    const val DEFAULT_TIMEZONE = "GMT"
    const val HOUR_FORMAT_12 = "hh:mm a"
}