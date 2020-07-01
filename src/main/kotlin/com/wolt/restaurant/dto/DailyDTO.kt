package com.wolt.restaurant.dto

import com.wolt.restaurant.util.WeekDays

data class DailyDTO(
     var day: WeekDays,
     var isWaitingForClosingTime: Boolean,
     var workingTimesList: MutableList<String>,
     var isPreviousDayExist: Boolean,
     var openingTimeInt: Int)