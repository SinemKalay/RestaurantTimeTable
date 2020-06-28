package com.wolt.restaurant

import com.wolt.restaurant.dtos.TypeValueDTO
import com.wolt.restaurant.utils.Constants
import com.wolt.restaurant.utils.WeekDays
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
class RestaurantController @Autowired constructor(
    private val service: RestaurantService
) {
    @PostMapping(path = [Constants.POST_URI], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun getReadableOpeningHours(@RequestBody() @Valid openingHoursInfo:
                                HashMap<WeekDays, List<TypeValueDTO>>): String {
        return service.analyzeMap(openingHoursInfo)
    }

}