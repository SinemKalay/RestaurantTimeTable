package com.wolt.restaurant

import com.wolt.restaurant.dto.WorkingTimesForADay
import com.wolt.restaurant.dto.TypeValueDTO
import com.wolt.restaurant.utils.WeekDays
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
class MainController @Autowired constructor(
        private val service: ProcessDataService
) {
    @PostMapping("/openingHours")
    fun getReadableOpeningHours(@RequestBody() openingHoursInfo:
            HashMap<WeekDays, List<TypeValueDTO>>): List<WorkingTimesForADay> {
        return service.analyzeHoursMap(openingHoursInfo)
    }

}