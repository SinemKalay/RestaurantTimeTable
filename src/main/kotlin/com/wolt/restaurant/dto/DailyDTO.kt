package com.wolt.restaurant.dto

import com.wolt.restaurant.util.WeekDays

class DailyDTO(
    internal var day: WeekDays,
    internal var isWaitingForClosingTime: Boolean,
    internal var workingTimesList: MutableList<String>,
    internal var isPreviousDayExist: Boolean,
    internal var openingTimeInt: Int)