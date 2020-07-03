package com.wolt.restaurant

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.wolt.restaurant.dto.DailyScheduleDTO
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
import java.util.Date
import java.util.TimeZone
import kotlin.collections.HashMap

@Service
class RestaurantService {

    private var weeklyScheduleList: MutableList<WorkingTimesForADayDTO> = mutableListOf()
    private var sundayClosingTime: Int = 0

    fun analyzeTimeTable(scheduleJsonStr: String): String {
        val dayToListMap = parseJsonToDTO(scheduleJsonStr)
        TypeValueValidator().validateScheduleInput(dayToListMap)
        getLogger().info("Restaurant working time data is sent to analyze...")
        analyzeSchedule(dayToListMap)

        return createReadableResponse()
    }

    private fun analyzeSchedule(dayToListMap: HashMap<WeekDays, List<TypeValueDTO>>) {
        var dailyDTO = DailyScheduleDTO(day = WeekDays.monday, workingTimesList = mutableListOf(),
            isWaitingForClosingTime = false, isPreviousDayExist = false, timeInt = 0)
        weeklyScheduleList = mutableListOf()
        getLogger().info("Narrow down loop according to input")
        val daysInInput = WeekDays.values().filter { d -> dayToListMap.containsKey(d) }

        getLogger("Schedule analyze operation has been started")
        for (currentDay in daysInInput) {
            val hoursList = dayToListMap.getValue(currentDay)
            if (hoursList.isEmpty()) {
                markDayAsClosed(currentDay)
            } else {
                val hasPreviousDay = hasPreviousDay(dayToListMap, currentDay)
                if (!dailyDTO.isWaitingForClosingTime)
                    dailyDTO = DailyScheduleDTO(currentDay, mutableListOf(), false,
                        hasPreviousDay,0)
                else {
                    dailyDTO.isPreviousDayExist = hasPreviousDay
                }
                dailyDTO = lookCloserToDay(Pair(currentDay, hoursList), dailyDTO)
            }
        }
        isStillWaitingClosingTime(dailyDTO)
        getLogger("Schedule analyze operation has been finished")
    }

    private fun lookCloserToDay(dayHoursListPair: Pair<WeekDays, List<TypeValueDTO>>,
            dailyDTO: DailyScheduleDTO): DailyScheduleDTO {
        val day = dayHoursListPair.first
        val hoursList = dayHoursListPair.second
        var localDailyDTO = dailyDTO

        getLogger("Look in detail to $day")
        for ((index, typeValueDTO) in hoursList.withIndex()) {
            when (typeValueDTO.type) {
                TypeEnum.open.name ->
                    localDailyDTO = openingTimeOperations(localDailyDTO, typeValueDTO.value)

                TypeEnum.close.name ->
                    localDailyDTO = closingTimeOperations(index, localDailyDTO, typeValueDTO.value)
            }
        }
        if (!localDailyDTO.isWaitingForClosingTime)
            addDailySchedule(localDailyDTO)

        return localDailyDTO
    }

    @Throws(UnmatchedOpenCloseTimeException::class)
    private fun openingTimeOperations(dailyDTO: DailyScheduleDTO,timeInt: Int): DailyScheduleDTO {
        if (dailyDTO.isWaitingForClosingTime)
            throw UnmatchedOpenCloseTimeException(Constants.EXP_MSG_UNEXP_OPENING,
                "Opening time information was not expected")
        val time = convertTimeFormat(timeInt)
        dailyDTO.isWaitingForClosingTime = true
        dailyDTO.workingTimesList.add("$time - ")
        dailyDTO.timeInt = timeInt
        getLogger("Opening time has been added for ${dailyDTO.day}")

        return dailyDTO
    }

    @Throws(UnmatchedOpenCloseTimeException::class, InaccurateTimingException::class)
    private fun closingTimeOperations(index: Int, dailyDTO: DailyScheduleDTO,
              closingTimeInt: Int): DailyScheduleDTO {
        if (dailyDTO.day == WeekDays.monday && dailyDTO.isPreviousDayExist){
            sundayClosingTime= closingTimeInt
            getLogger().info("This closing info will be recorded as Sunday closing time")
            return dailyDTO
        }
        checkClosingTimeException(index, dailyDTO, closingTimeInt)
        val updatedDailyDTO = addClosingTimeToDTO(dailyDTO, closingTimeInt)

        return checkOverNightTime(index, updatedDailyDTO)
    }

    private fun checkOverNightTime(index: Int, dailyDTO: DailyScheduleDTO): DailyScheduleDTO{
        if (index == 0 && dailyDTO.day != WeekDays.monday) {
            addDailySchedule(dailyDTO)
            dailyDTO.workingTimesList = mutableListOf()
            if(dailyDTO.day.ordinal < WeekDays.values().lastIndex)
                dailyDTO.day = WeekDays.values()[dailyDTO.day.ordinal+1]
            else
                dailyDTO.day = WeekDays.monday
            getLogger().warn("Opening-Closing time are not on same day")
        }
        return dailyDTO
    }

    private fun addClosingTimeToDTO(dailyDTO: DailyScheduleDTO, closingTimeInt: Int): DailyScheduleDTO {
        val lastIndex = dailyDTO.workingTimesList.lastIndex
        val openingTime = dailyDTO.workingTimesList.last()
        val closingTime = convertTimeFormat(closingTimeInt)
        dailyDTO.workingTimesList[lastIndex] = openingTime + closingTime
        dailyDTO.isWaitingForClosingTime = false

        return dailyDTO
    }

    private fun checkClosingTimeException(index: Int, dailyDTO: DailyScheduleDTO,
                closingTimeInt: Int){
        if (!dailyDTO.isWaitingForClosingTime)
            throw UnmatchedOpenCloseTimeException(Constants.EXP_MSG_UNEXP_CLOSING,
                "Closing time information was not expected")
        if (index == 0 && !dailyDTO.isPreviousDayExist)
            throw UnmatchedOpenCloseTimeException(Constants.EXP_MSG_NON_SEQUENTIAL,
                "Opening-Closing times must be on same or sequential day")

        val openingTimeInt = dailyDTO.timeInt
        if (index !=0 && openingTimeInt > closingTimeInt)
            throw InaccurateTimingException(Constants.EXP_MSG_INACCURATE_TIMING,
                "Opening time can not be later than closing time")
    }

    private fun hasPreviousDay(hoursMap: HashMap<WeekDays, List<TypeValueDTO>>,
               day: WeekDays): Boolean {
        return if (day.ordinal == 0) {
            hoursMap.containsKey(WeekDays.sunday)
        } else {
            hoursMap.containsKey(WeekDays.values()[day.ordinal - 1])
        }
    }

    private fun isStillWaitingClosingTime(dailyDTO: DailyScheduleDTO) {
        if (sundayClosingTime!= 0 && dailyDTO.isWaitingForClosingTime){
            addSundayOverNightSchedule(dailyDTO, sundayClosingTime)
        }
        if (dailyDTO.isWaitingForClosingTime)
            throw UnmatchedOpenCloseTimeException(Constants.EXP_MSG_WAIT_CLOSING,
                "Closing time was waited for opening time at the end")
    }

    private fun addSundayOverNightSchedule(sundayDailyDTO: DailyScheduleDTO,
               sundayOvertime: Int) {
        val closingTime = convertTimeFormat(sundayOvertime)
        val lastIndex = sundayDailyDTO.workingTimesList.lastIndex
        sundayDailyDTO.workingTimesList[lastIndex] += closingTime
        sundayDailyDTO.isWaitingForClosingTime = false
        addDailySchedule(sundayDailyDTO)
    }

    private fun markDayAsClosed(day: WeekDays) {
        val workingTimesList = listOf(Constants.ALL_DAY_CLOSED)
        val workingTimesForADay = WorkingTimesForADayDTO(day, workingTimesList)
        weeklyScheduleList.add(workingTimesForADay)
        getLogger().info("Restaurant has been marked all day closed on $day")
    }

    private fun addDailySchedule(dailyDTO: DailyScheduleDTO) {
        val workingTimesForADay = WorkingTimesForADayDTO(dailyDTO.day, dailyDTO.workingTimesList)
        weeklyScheduleList.add(workingTimesForADay)
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

    private fun createReadableResponse(): String {
        val respStrBuilder = StringBuilder("Restaurant is open:\n")
        for (dailyHours in weeklyScheduleList) {
            respStrBuilder.append("${dailyHours.day.name.capitalize()}: ")
            if(dailyHours.workingIntervalList.isEmpty()){
                respStrBuilder.append("Closed")
            }
            else {
                for ((index, period) in dailyHours.workingIntervalList.withIndex()) {
                    respStrBuilder.append(period)
                    if (index < dailyHours.workingIntervalList.size - 1)
                        respStrBuilder.append(", ")
                }
            }
            respStrBuilder.append("\n")
        }
        getLogger().info("Restaurant's working hours has been converted to readable format")
        return respStrBuilder.toString()
    }
}