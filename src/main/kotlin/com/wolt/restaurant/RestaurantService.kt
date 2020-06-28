package com.wolt.restaurant

import com.wolt.restaurant.dtos.DailyDTO
import com.wolt.restaurant.dtos.TypeValueDTO
import com.wolt.restaurant.dtos.WorkingTimesForADayDTO
import com.wolt.restaurant.exceptions.InaccurateTimingException
import com.wolt.restaurant.exceptions.UnmatchedOpenCloseTimeException
import com.wolt.restaurant.utils.Constants
import com.wolt.restaurant.utils.Type
import com.wolt.restaurant.utils.WeekDays
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.util.TimeZone
import java.util.Date
import kotlin.collections.HashMap

@Service
class RestaurantService {

    private var readableWorkingTimesListDTO: MutableList<WorkingTimesForADayDTO> = mutableListOf()

    fun analyzeMap(hoursMap: HashMap<WeekDays, List<TypeValueDTO>>): String {
        readableWorkingTimesListDTO = mutableListOf()
        analyzeWorkingHours(hoursMap)
        return createReadableResponse(readableWorkingTimesListDTO)
    }

    private fun analyzeWorkingHours(hoursMap: HashMap<WeekDays, List<TypeValueDTO>>) {
        val daysInInput = WeekDays.values().filter { d -> hoursMap.containsKey(d) }
        var dailyDTO = DailyDTO(WeekDays.monday, false, mutableListOf(), false, 0)

        for (day in daysInInput) {
            val hoursList = hoursMap.getValue(day)
            if (hoursList.isEmpty()) {
                markDayAsClosed(day)
            } else {
                val isPreviousDayExist = hoursMap.containsKey(WeekDays.values()[day.ordinal - 1])
                dailyDTO.isPreviousDayExist = isPreviousDayExist
                if (!dailyDTO.isWaitingForClosingTime)
                    dailyDTO = DailyDTO(day, false, mutableListOf(), isPreviousDayExist,0)
                dailyDTO = lookCloserToHours(Pair(day, hoursList), dailyDTO)
            }
        }
    }

    private fun lookCloserToHours(dayHoursPair: Pair<WeekDays, List<TypeValueDTO>>,
            dailyDTO: DailyDTO): DailyDTO {
        val day = dayHoursPair.first
        var hoursList = dayHoursPair.second
        var localDailyDTO = dailyDTO

        for ((index, typeValueDTO) in hoursList.withIndex()) {
            when (typeValueDTO.type) {
                Type.open -> localDailyDTO = openingTimeOperations(localDailyDTO,typeValueDTO.value)
                Type.close -> {
                    localDailyDTO = closingTimeOperations(index, localDailyDTO, typeValueDTO.value)
                    if (index == 0) {
                        addDailyShift(localDailyDTO)
                        localDailyDTO.workingTimesList = mutableListOf()
                        localDailyDTO.day = day
                    }
                }
            }
        }
        if (!localDailyDTO.isWaitingForClosingTime)
            addDailyShift(localDailyDTO)

        return localDailyDTO
    }

    @Throws(UnmatchedOpenCloseTimeException::class)
    private fun openingTimeOperations(dailyDTO: DailyDTO, timeInt: Int): DailyDTO {
        if (dailyDTO.isWaitingForClosingTime)
            throw UnmatchedOpenCloseTimeException(Constants.EXP_MSG_UNEXP_OPENING)
        var time = convertTimeFormat(timeInt)
        dailyDTO.isWaitingForClosingTime = true
        dailyDTO.workingTimesList.add("$time - ")
        dailyDTO.openingTimeInt = timeInt

        return dailyDTO
    }

    @Throws(UnmatchedOpenCloseTimeException::class)
    private fun closingTimeOperations(index: Int, dailyDTO: DailyDTO, closingTimeInt: Int): DailyDTO {
        if (!dailyDTO.isWaitingForClosingTime)
            throw UnmatchedOpenCloseTimeException(Constants.EXP_MSG_UNEXP_CLOSING)
        if (index == 0 && !dailyDTO.isPreviousDayExist)
            throw UnmatchedOpenCloseTimeException(Constants.EXP_MSG_NON_SEQUENTIAL)
        val openingTimeInt = dailyDTO.openingTimeInt
        if (index !=0 && openingTimeInt > closingTimeInt)
            throw InaccurateTimingException(Constants.EXP_MSG_INACCURATE_TIMING)

        val closingTime = convertTimeFormat(closingTimeInt)
        val lastIndex = dailyDTO.workingTimesList.lastIndex
        val openingTime = dailyDTO.workingTimesList.last()
        dailyDTO.workingTimesList[lastIndex] = openingTime + closingTime
        dailyDTO.isWaitingForClosingTime = false

        return dailyDTO
    }

    private fun markDayAsClosed(day: WeekDays) {
        val workingTimesList = listOf(Constants.ALL_DAY_CLOSED)
        val workingTimesForADay = WorkingTimesForADayDTO(day, workingTimesList)
        readableWorkingTimesListDTO.add(workingTimesForADay)
    }

    private fun addDailyShift(dailyDTO: DailyDTO) {
        val workingTimesForADay = WorkingTimesForADayDTO(dailyDTO.day, dailyDTO.workingTimesList)
        readableWorkingTimesListDTO.add(workingTimesForADay)
    }

    private fun convertTimeFormat(sec: Int): String {
        val df = SimpleDateFormat(Constants.HOUR_FORMAT_12)
        df.timeZone = TimeZone.getTimeZone(Constants.DEFAULT_TIMEZONE)
        return df.format(Date(sec * 1000L))
    }

    private fun createReadableResponse(workingTimesForDays: List<WorkingTimesForADayDTO>): String {
        var responseString = "Restaurant is open:\n"
        for (dailyHours in workingTimesForDays) {
            responseString += "${dailyHours.day.name.capitalize()}: "
            for ((index, period) in dailyHours.workingIntervalList.withIndex()) {
                responseString += period
                if (index < dailyHours.workingIntervalList.size - 1)
                    responseString += ", "
            }
            responseString += "\n"
        }
        return responseString
    }
}