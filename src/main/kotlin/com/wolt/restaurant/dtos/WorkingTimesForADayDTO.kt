package com.wolt.restaurant.dtos

import com.wolt.restaurant.utils.WeekDays

data class WorkingTimesForADayDTO(
    var day: WeekDays,
    var workingIntervalList: List<String>)