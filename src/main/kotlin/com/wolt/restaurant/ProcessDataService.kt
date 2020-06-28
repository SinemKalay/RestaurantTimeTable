package com.wolt.restaurant

import com.wolt.restaurant.dto.WorkingTimesForADay
import com.wolt.restaurant.dto.TypeValueDTO
import com.wolt.restaurant.utils.WeekDays
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


@Service
class ProcessDataService {


    fun analyzeHoursMap(hoursMap: HashMap<WeekDays, List<TypeValueDTO>>)
            : List<WorkingTimesForADay> {
        var readableWorkingTimesList: MutableList<WorkingTimesForADay> = mutableListOf()
        var workingTimesList: MutableList<String> = mutableListOf()
        var waitingForClosingTime = false
        var waitingForClosingDay = WeekDays.monday
        val daysInInput= WeekDays.values().filter { d -> hoursMap.containsKey(d) }

        for (day in daysInInput) {
            val hoursList= hoursMap.getValue(day)
            // if restaurant is close whole day
            if (hoursList.isEmpty()) {
                workingTimesList.add("Closed")
                val workingTimesForADay=  WorkingTimesForADay(day, workingTimesList)
                readableWorkingTimesList.add(workingTimesForADay)
            }
            else {
                for ((index, typeValueDTO) in hoursList.withIndex()) {
                    var time = convertTimeFormat(typeValueDTO.value)
                    if (typeValueDTO.type == TypeValueDTO.Type.open) {
                        workingTimesList.add("$time - ")
                        waitingForClosingTime = true
                        waitingForClosingDay = day

                    } else {
                        var lastItem = workingTimesList.last()
                        workingTimesList[workingTimesList.lastIndex] = lastItem + time

                        if (waitingForClosingTime && index==0) {
                            if(!hoursMap.containsKey(WeekDays.values()[day.ordinal-1])){
                                throw error("UnmatchedOpenCloseTimeException")
                            }
                            val workingTimesForADay=  WorkingTimesForADay(waitingForClosingDay, workingTimesList)
                            readableWorkingTimesList.add(workingTimesForADay)
                            workingTimesList= mutableListOf()
                        }
                        waitingForClosingTime = false
                    }
                }
                if (!waitingForClosingTime && workingTimesList.isNotEmpty()) {
                    val workingTimesForADay=  WorkingTimesForADay(waitingForClosingDay, workingTimesList)
                    readableWorkingTimesList.add(workingTimesForADay)
                    workingTimesList= mutableListOf()
                }
            }
        }
        return readableWorkingTimesList
    }

    private fun convertTimeFormat(sec: Int): String {
        val df = SimpleDateFormat("hh:mm a")
        df.timeZone = TimeZone.getTimeZone("GMT")
        return df.format(Date(sec * 1000L))
    }

}