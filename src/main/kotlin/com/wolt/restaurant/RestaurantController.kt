package com.wolt.restaurant

import com.wolt.restaurant.util.Constants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class RestaurantController @Autowired constructor(
    private val service: RestaurantService) {

    @PostMapping(path = [Constants.POST_URI], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun postTimetable(@RequestBody timetableJson: String): ResponseEntity<String> {
        val readableSchedule = service.formatThenReturnTimetable(timetableJson)
        return ResponseEntity.ok(readableSchedule)
    }
}