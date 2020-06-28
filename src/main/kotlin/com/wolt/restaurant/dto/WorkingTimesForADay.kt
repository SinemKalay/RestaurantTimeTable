package com.wolt.restaurant.dto

import com.wolt.restaurant.utils.WeekDays

data class WorkingTimesForADay(
        var day: WeekDays,
        var workingIntervalList: List<String>)