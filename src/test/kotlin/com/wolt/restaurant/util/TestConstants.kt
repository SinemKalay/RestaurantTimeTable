package com.wolt.restaurant.util

object TestConstants {
    const val POST_URI = "/restaurantTimetable"
    const val JSON_REQUESTS_PATH = "classpath:/static/requestJsonFile/"
    const val JSON_RESPONSES_PATH = "classpath:/static/responseFile/"
    const val EXP_MSG_UNEXP_OPENING = "Unexpected opening time!"
    const val EXP_MSG_UNEXP_CLOSING = "Unexpected closing time!"
    const val EXP_MSG_NON_SEQUENTIAL = "Entered days in working times info are not sequential!"
    const val EXP_MSG_UNMATCHED_OPEN_CLOSE = "Number of opening and closing times do not overlap"
    const val EXP_MSG_INACCURATE_TIMING = "Corrupted time value!"
    const val EXP_MSG_TIMING_CONSTRAINTS = "Value field should be exist and in between 0 and 86400."
    const val EXP_MSG_VALUE_NOT_FOUND = "Time value not found!"
    const val EXP_MSG_TYPE_NOT_FOUND = "Type value not found!"
    const val EXP_MSG_NO_SUCH_DAY = "No such day found."
    const val EXP_MSG_NO_SUCH_TYPE = "No such type found."
    const val ERROR_STATUS = "status"
    const val ERROR_NAME = "error"
    const val ERROR_MSG = "message"
    const val MEDIA_TYPE = "application/json;charset=UTF-8"
}