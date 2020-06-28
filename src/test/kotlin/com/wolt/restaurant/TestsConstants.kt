package com.wolt.restaurant

object TestsConstants {
    internal const val POST_URI = "/openingHours"
    internal const val JSON_REQUESTS_PATH = "classpath:/static/requests/"
    internal const val JSON_RESPONSES_PATH = "classpath:/static/responses/"
    internal const val JSON_FILE_PATH_WHOLE_WEEK= "WholeWeekInfoRequest.json"
    internal const val RESP_FILE_PATH_WHOLE_WEEK= "WholeWeekResponse"
    internal const val JSON_FILE_PATH_PARTLY_WEEK= "PartOfWeekInfoRequest.json"
    internal const val RESP_FILE_PATH_PARTLY_WEEK= "PartOfWeekResponse"
    internal const val JSON_FILE_PATH_INACCURATE_TIMING= "InaccurateTimingRequest.json"
    internal const val JSON_FILE_PATH_UNEXP_OPENING= "UnexpectedOpeningTimeRequest.json"
    internal const val JSON_FILE_PATH_UNEXP_CLOSING= "UnexpectedClosingTimeRequest.json"
    internal const val JSON_FILE_PATH_NON_SEQUENTIAL= "DaysNotSequentialRequest.json"
    internal const val EXP_MSG_UNEXP_OPENING = "Unexpected opening time!"
    internal const val EXP_MSG_UNEXP_CLOSING = "Unexpected closing time!"
    internal const val EXP_MSG_NON_SEQUENTIAL = "Entered days in working times info are not sequential!"
    internal const val EXP_MSG_INACCURATE_TIMING = "Opening time and closing time incompatible!"
    internal const val ERROR_CODE = "errorCode"
    internal const val ERROR_MSG = "errorMessage"
    internal const val MEDIA_TYPE = "application/json;charset=UTF-8"
}