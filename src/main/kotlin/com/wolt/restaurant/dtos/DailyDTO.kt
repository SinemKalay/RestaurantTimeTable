package com.wolt.restaurant.dtos

import com.wolt.restaurant.utils.WeekDays

class DailyDTO(
    internal var day: WeekDays,
    internal var isWaitingForClosingTime: Boolean,
    internal var workingTimesList: MutableList<String>,
    internal var isPreviousDayExist: Boolean,
    internal var openingTimeInt: Int)