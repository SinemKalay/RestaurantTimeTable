package com.wolt.restaurant

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.wolt.restaurant.dto.DailyDTO
import com.wolt.restaurant.dto.TypeValueDTO
import com.wolt.restaurant.dto.WorkingTimesForADayDTO
import com.wolt.restaurant.exception.InaccurateTimingException
import com.wolt.restaurant.exception.RespBodyNotFoundException
import com.wolt.restaurant.exception.UnmatchedOpenCloseTimeException
import com.wolt.restaurant.util.Constants
import com.wolt.restaurant.util.TypeEnum
import com.wolt.restaurant.util.TypeValueValidator
import com.wolt.restaurant.util.WeekDays
import com.wolt.restaurant.util.getLogger
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.util.*

@Service
class RestaurantService {

    private var readableWorkingTimesListDTO: MutableList<WorkingTimesForADayDTO> = mutableListOf()

    fun analyzeTimeTable(jsonStr: String): String {
        val hoursMap = parseJsonToDTO(jsonStr)
        if(TypeValueValidator().isValidatedOk(hoursMap)) {
            getLogger().info("Restaurant working time data is sent to analyze...")
            analyzeWorkingHours(hoursMap)
        }

        return createReadableResponse(readableWorkingTimesListDTO)
    }

    private fun analyzeWorkingHours(hoursMap: HashMap<WeekDays, List<TypeValueDTO>>) {
        val daysInInput = WeekDays.values().filter { d -> hoursMap.containsKey(d) }
        var dailyDTO = DailyDTO(WeekDays.monday, false,
            mutableListOf(), false, 0)
        readableWorkingTimesListDTO = mutableListOf()

        for (day in daysInInput) {
            val hoursList = hoursMap.getValue(day)
            if (hoursList.isEmpty()) {
                markDayAsClosed(day)
            } else {
                val isPreviousDayExist = hoursMap.containsKey(WeekDays.values()[day.ordinal - 1])
                dailyDTO.isPreviousDayExist = isPreviousDayExist
                if (!dailyDTO.isWaitingForClosingTime)
                    dailyDTO = DailyDTO(day, false,
                        mutableListOf(), isPreviousDayExist,0)
                dailyDTO = lookCloserToHours(Pair(day, hoursList), dailyDTO)
            }
        }
        isStillWaitingClosingTime(dailyDTO)
    }

    private fun isStillWaitingClosingTime(dailyDTO: DailyDTO) {
        if(dailyDTO.isWaitingForClosingTime)
            throw UnmatchedOpenCloseTimeException(Constants.EXP_MSG_WAIT_CLOSING,
                "Closing time was waited for opening time at the end")
    }

    private fun lookCloserToHours(dayHoursPair: Pair<WeekDays, List<TypeValueDTO>>,
            dailyDTO: DailyDTO): DailyDTO {
        val day = dayHoursPair.first
        var hoursList = dayHoursPair.second
        var localDailyDTO = dailyDTO

        for ((index, typeValueDTO) in hoursList.withIndex()) {
            when (typeValueDTO.type) {
                TypeEnum.open -> localDailyDTO = openingTimeOperations(localDailyDTO,typeValueDTO.value)
                TypeEnum.close -> {
                    localDailyDTO = closingTimeOperations(index, localDailyDTO, typeValueDTO.value)
                    if (index == 0) {
                        addDailyShift(localDailyDTO)
                        localDailyDTO.workingTimesList = mutableListOf()
                        localDailyDTO.day = day
                        getLogger().warn("Opening-Closing time are not on same day")
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
            throw UnmatchedOpenCloseTimeException(Constants.EXP_MSG_UNEXP_OPENING,
                "Opening time information was not expected")
        var time = convertTimeFormat(timeInt)
        dailyDTO.isWaitingForClosingTime = true
        dailyDTO.workingTimesList.add("$time - ")
        dailyDTO.openingTimeInt = timeInt

        return dailyDTO
    }

    @Throws(UnmatchedOpenCloseTimeException::class, InaccurateTimingException::class)
    private fun closingTimeOperations(index: Int, dailyDTO: DailyDTO, closingTimeInt: Int): DailyDTO {
        if (!dailyDTO.isWaitingForClosingTime)
            throw UnmatchedOpenCloseTimeException(Constants.EXP_MSG_UNEXP_CLOSING,
                "Closing time information was not expected")
        if (index == 0 && !dailyDTO.isPreviousDayExist)
            throw UnmatchedOpenCloseTimeException(Constants.EXP_MSG_NON_SEQUENTIAL,
                "Opening-Closing times must be on same or sequential day")

        val openingTimeInt = dailyDTO.openingTimeInt
        if (index !=0 && openingTimeInt > closingTimeInt)
            throw InaccurateTimingException(Constants.EXP_MSG_INACCURATE_TIMING,
                "Opening time can not be later than closing time")

        val lastIndex = dailyDTO.workingTimesList.lastIndex
        val openingTime = dailyDTO.workingTimesList.last()
        val closingTime = convertTimeFormat(closingTimeInt)
        dailyDTO.workingTimesList[lastIndex] = openingTime + closingTime
        dailyDTO.isWaitingForClosingTime = false

        return dailyDTO
    }

    private fun markDayAsClosed(day: WeekDays) {
        val workingTimesList = listOf(Constants.ALL_DAY_CLOSED)
        val workingTimesForADay = WorkingTimesForADayDTO(day, workingTimesList)
        readableWorkingTimesListDTO.add(workingTimesForADay)
        getLogger().info("Restaurant has been marked all day closed on $day")
    }

    private fun addDailyShift(dailyDTO: DailyDTO) {
        val workingTimesForADay = WorkingTimesForADayDTO(dailyDTO.day, dailyDTO.workingTimesList)
        readableWorkingTimesListDTO.add(workingTimesForADay)
        getLogger().info("Daily working hours has been added for ${dailyDTO.day}")
    }


    @Throws(JsonSyntaxException::class, RespBodyNotFoundException::class)
    private fun parseJsonToDTO(jsonStr: String): HashMap<WeekDays, List<TypeValueDTO>> {
        val gson = Gson()
        val convertedObj: HashMap<WeekDays, List<TypeValueDTO>> =
            gson.fromJson(jsonStr, object : TypeToken<HashMap<WeekDays, List<TypeValueDTO>>>() {}.type)
        if(convertedObj.size==0)
            throw RespBodyNotFoundException(Constants.EXP_MSG_RESP_BODY_NOT_FOUND,
                Constants.REASON_RESP_BODY_NOT_FOUND)
        getLogger().info("Json string converted to HashMap successfully")

        return convertedObj
    }

    private fun convertTimeFormat(sec: Int): String {
        val df = SimpleDateFormat(Constants.HOUR_FORMAT_12)
        df.timeZone = TimeZone.getTimeZone(Constants.DEFAULT_TIMEZONE)
        return df.format(Date(sec * 1000L))
    }

    private fun createReadableResponse(workingTimesForDays: List<WorkingTimesForADayDTO>): String {
        val respStrBuilder = StringBuilder("Restaurant is open:\n");
        for (dailyHours in workingTimesForDays) {
            respStrBuilder.append("${dailyHours.day.name.capitalize()}: ")
            for ((index, period) in dailyHours.workingIntervalList.withIndex()) {
                respStrBuilder.append(period)
                if (index < dailyHours.workingIntervalList.size - 1)
                    respStrBuilder.append(", ")
            }
            respStrBuilder.append("\n")
        }
        getLogger().info("Restaurant's working hours has been converted to readable format")
        return respStrBuilder.toString()
    }
}