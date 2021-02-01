package com.wolt.restaurant.dto

import com.wolt.restaurant.util.WeekDays

data class DayIntervalsDTO(
    var day: WeekDays,
    var intervalList: List<String>)