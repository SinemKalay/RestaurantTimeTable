package com.wolt.restaurant.dto

import com.wolt.restaurant.util.WeekDays

data class WorkingTimesForADayDTO(
    var day: WeekDays,
    var workingIntervalList: List<String>)