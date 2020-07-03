package com.wolt.restaurant.dto

import com.wolt.restaurant.util.WeekDays

data class DailyScheduleDTO(
    var day: WeekDays,
    var workingTimesList: MutableList<String>,
    var isWaitingForClosingTime: Boolean,
    var isPreviousDayExist: Boolean,
    var timeInt: Int)