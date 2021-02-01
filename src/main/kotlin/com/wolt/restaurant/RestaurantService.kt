package com.wolt.restaurant

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.wolt.restaurant.dto.DayIntervalsDTO
import com.wolt.restaurant.dto.TypeValueDTO
import com.wolt.restaurant.exception.RespBodyNotFoundException
import com.wolt.restaurant.exception.UnmatchedOpenCloseTimeException
import com.wolt.restaurant.util.Constants
import com.wolt.restaurant.util.TypeEnum
import com.wolt.restaurant.util.TypeValueValidator
import com.wolt.restaurant.util.WeekDays
import com.wolt.restaurant.util.getLogger
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

@Service
class RestaurantService {

    fun processTimetableReturnReadableSchedule(timetableJsonStr: String): String {
        val mapDayIntervalList = convertJsonBodyToHashMap(timetableJsonStr)
        val weeklyScheduleList = getWeeklySchedule(mapDayIntervalList)

        return getReadableSchedule(weeklyScheduleList)
    }

    fun convertJsonBodyToHashMap(timetableJsonStr: String): HashMap<WeekDays, List<TypeValueDTO>> {
        getLogger().info("Parse json input to TypeValueDTO")
        val mapDayIntervalList = parseJsonToDTO(timetableJsonStr)
        TypeValueValidator().validateTimetableInput(mapDayIntervalList)

        return mapDayIntervalList
    }

    private fun getWeeklySchedule(mapDayIntervalList: HashMap<WeekDays, List<TypeValueDTO>>)
            : MutableList<DayIntervalsDTO> {
        val weeklyScheduleList: MutableList<DayIntervalsDTO> = mutableListOf()
        val daysInTimetable = WeekDays.values().filter { d -> mapDayIntervalList.containsKey(d) }
        val mapDaySortedHoursList = sortTimeValues(mapDayIntervalList, daysInTimetable)
        for (currentDay in daysInTimetable.reversed()) {
            val hoursList = mapDayIntervalList.getValue(currentDay)
            if (hoursList.isEmpty()) {
                weeklyScheduleList.add(markDayAsClosed(currentDay))
            } else {
                val listIntervalsOfCurrentDay = getIntervalsList(currentDay, mapDaySortedHoursList)
                val dayIntervalsDTO = DayIntervalsDTO(currentDay, listIntervalsOfCurrentDay)
                weeklyScheduleList.add(dayIntervalsDTO)
            }
        }
        getLogger("Schedule analyze operation has been finished")
        return weeklyScheduleList.reversed().toMutableList()
    }

    private fun sortTimeValues(dayToListMap: HashMap<WeekDays, List<TypeValueDTO>>,
            daysInInputString: List<WeekDays>): HashMap<WeekDays, List<TypeValueDTO>> {
        for (currentDay in daysInInputString) {
            if (!dayToListMap[currentDay].isNullOrEmpty()) {
                val sortedHoursList = dayToListMap[currentDay]!!.sortedBy { it.value }
                dayToListMap[currentDay] = sortedHoursList
            }
        }
        return dayToListMap
    }

    private fun getIntervalsList(currentDay: WeekDays, mapDaySortedHoursListParam:
            HashMap<WeekDays, List<TypeValueDTO>>): MutableList<String> {
        var openingTimeInSeconds = -1
        var intervalList: MutableList<String> = mutableListOf()
        var mapDaySortedHoursList = mapDaySortedHoursListParam

        getLogger("Go through intervals of $currentDay")
        for (typeValueDTO in mapDaySortedHoursList[currentDay]!!) {
            when (typeValueDTO.type) {
                TypeEnum.open.name -> {
                    val (intervalListTemp, openingTimeInSecondsTemp) =
                        whenTypeIsOpen(currentDay, mapDaySortedHoursList, typeValueDTO,
                            intervalList, openingTimeInSeconds)
                    intervalList = intervalListTemp
                    openingTimeInSeconds = openingTimeInSecondsTemp
                }
                TypeEnum.close.name -> {
                    val (mapDaySortedHoursListTemp, intervalListTemp, openingTimeInSecondsTemp) =
                        whenTypeIsClose(currentDay, mapDaySortedHoursList, typeValueDTO,
                            intervalList, openingTimeInSeconds)
                    mapDaySortedHoursList = mapDaySortedHoursListTemp
                    intervalList = intervalListTemp
                    openingTimeInSeconds = openingTimeInSecondsTemp
                }
            }
        }
        getLogger("Intervals of $currentDay processed")
        return intervalList
    }

    private fun whenTypeIsOpen(currentDay: WeekDays, mapDaySortedHoursList: HashMap<WeekDays, List<TypeValueDTO>>,
            typeValueDTO: TypeValueDTO, intervalList: MutableList<String>, openingTimeInSeconds: Int):
            Pair<MutableList<String>, Int> {
        var openingTimeInSecondsTemp = openingTimeInSeconds
        if (typeValueDTO == mapDaySortedHoursList[currentDay]!!.last()
            && isClosingTimeInfoOnNextDay(mapDaySortedHoursList, currentDay)) {
            val nextDay = getNextDay(currentDay)
            val closingTimeInfoOnNextDay = mapDaySortedHoursList[nextDay]!!.first()
            intervalList.add(returnInterval(typeValueDTO.value, closingTimeInfoOnNextDay.value))
            openingTimeInSecondsTemp = -1
        } else {
            if (openingTimeInSecondsTemp != -1)
                throw UnmatchedOpenCloseTimeException(Constants.EXP_MSG_UNEXP_OPENING,
                    "Unexpected opening time on $currentDay")
            openingTimeInSecondsTemp = typeValueDTO.value
        }
        return Pair(intervalList, openingTimeInSecondsTemp)
    }

    @Throws(UnmatchedOpenCloseTimeException::class)
    private fun whenTypeIsClose(currentDay: WeekDays, mapDaySortedHoursList: HashMap<WeekDays, List<TypeValueDTO>>,
            typeValueDTO: TypeValueDTO, intervalList: MutableList<String>, openingTimeInSeconds: Int):
            Triple<HashMap<WeekDays, List<TypeValueDTO>>, MutableList<String>, Int> {
        var openingTimeInSecondsTemp = openingTimeInSeconds
        if (typeValueDTO == mapDaySortedHoursList[currentDay]!!.first()
            && isPreviousDayClosingTimeInfoOnCurrentDay(mapDaySortedHoursList, currentDay)) {
            val prevDay = getPreviousDay(currentDay)
            var tempList = mapDaySortedHoursList[prevDay]!!.toMutableList()
            typeValueDTO.isOvertime = true
            tempList.add(typeValueDTO)
            mapDaySortedHoursList[prevDay] = tempList
            tempList = mapDaySortedHoursList[currentDay]!!.toMutableList()
            tempList.removeAt(0)
            mapDaySortedHoursList[currentDay] = tempList
        } else {
            if (openingTimeInSecondsTemp == -1)
                throw UnmatchedOpenCloseTimeException(Constants.EXP_MSG_UNEXP_CLOSING,
                    "Closing time information was not expected on $currentDay")
            intervalList.add(returnInterval(openingTimeInSecondsTemp, typeValueDTO.value))
            openingTimeInSecondsTemp = -1
        }
        return Triple(mapDaySortedHoursList, intervalList, openingTimeInSecondsTemp)
    }

    private fun returnInterval(openingTimeSecond: Int, closingTimeSecond: Int): String {
        val openingTime = convertTimeFormat(openingTimeSecond.toLong()) + " - "
        val closingTime = convertTimeFormat(closingTimeSecond.toLong())
        return "$openingTime$closingTime"
    }

    private fun getNextDay(currentDay: WeekDays): WeekDays {
        return if (currentDay == WeekDays.sunday)
            WeekDays.monday
        else
            WeekDays.values()[currentDay.ordinal + 1]
    }

    private fun getPreviousDay(currentDay: WeekDays): WeekDays {
        return if (currentDay == WeekDays.monday)
            WeekDays.sunday
        else
            WeekDays.values()[currentDay.ordinal - 1]
    }

    @Throws(UnmatchedOpenCloseTimeException::class)
    private fun isPreviousDayClosingTimeInfoOnCurrentDay(mapDaySortedHoursList: HashMap<WeekDays,
        List<TypeValueDTO>>, currentDay: WeekDays): Boolean {
        val previousDay = getPreviousDay(currentDay)
        if (mapDaySortedHoursList.containsKey(previousDay)
            && mapDaySortedHoursList[previousDay]!!.isNotEmpty()) {
            if (mapDaySortedHoursList[previousDay]!!.last().type != TypeEnum.open.toString())
                throw UnmatchedOpenCloseTimeException(Constants.EXP_MSG_UNEXP_CLOSING,
                    "Unexpected closing time on $currentDay")
        } else {
            throw UnmatchedOpenCloseTimeException(Constants.EXP_MSG_NON_SEQUENTIAL,
                "Opening-Closing times must be on same or sequential day")
        }
        return true
    }

    @Throws(UnmatchedOpenCloseTimeException::class)
    private fun isClosingTimeInfoOnNextDay(mapDaySortedHoursList: HashMap<WeekDays, List<TypeValueDTO>>,
        currentDay: WeekDays): Boolean {
        val nextDay = getNextDay(currentDay)
        if (mapDaySortedHoursList.containsKey(nextDay)
            && mapDaySortedHoursList[nextDay]!!.isNotEmpty()) {
            if (mapDaySortedHoursList[nextDay]!!.first().type != TypeEnum.close.toString())
                throw UnmatchedOpenCloseTimeException(Constants.EXP_MSG_UNEXP_OPENING,
                    "Unexpected opening time on $currentDay")
        } else {
            throw UnmatchedOpenCloseTimeException(Constants.EXP_MSG_UNEXP_OPENING,
                "Unclosed day exists $currentDay")
        }
        return true
    }

    private fun markDayAsClosed(day: WeekDays): DayIntervalsDTO {
        val dayIntervalsDTO = DayIntervalsDTO(day, listOf(Constants.ALL_DAY_CLOSED))
        getLogger().info("Restaurant has been marked all day closed on $day")
        return dayIntervalsDTO
    }

    @Throws(JsonSyntaxException::class, RespBodyNotFoundException::class)
    private fun parseJsonToDTO(jsonStr: String): HashMap<WeekDays, List<TypeValueDTO>> {
        val gson = Gson()
        val convertedObj: HashMap<WeekDays, List<TypeValueDTO>> =
            gson.fromJson(jsonStr, object : TypeToken<HashMap<WeekDays, List<TypeValueDTO>>>() {}.type)
        if (convertedObj.size == 0)
            throw RespBodyNotFoundException(Constants.EXP_MSG_RESP_BODY_NOT_FOUND,
                Constants.REASON_RESP_BODY_NOT_FOUND)
        getLogger().info("Json string converted to HashMap successfully")

        return convertedObj
    }

    private fun convertTimeFormat(sec: Long): String {
        val hours: Long = TimeUnit.SECONDS.toHours(sec)
        val minutes: Long = TimeUnit.SECONDS.toMinutes(sec) - TimeUnit.HOURS.toMinutes(hours)
        val seconds: Long = TimeUnit.SECONDS.toSeconds(sec) - TimeUnit.HOURS.toSeconds(hours) -
            TimeUnit.MINUTES.toSeconds(minutes)
        var timeStr = getHourStr(hours)
        timeStr += getMinuteStr(minutes, seconds)
        timeStr += getSecondsStr(seconds)
        timeStr += if (hours < 12) " AM" else " PM"
        getLogger().info("$sec is converted into $timeStr")
        return timeStr
    }

    private fun getHourStr(hours: Long): String {
        return when {
            hours == 0L -> "12"
            hours > 12L -> (hours % 12).toString()
            else -> hours.toString()
        }
    }

    private fun getMinuteStr(minutes: Long, seconds: Long): String {
        return if (minutes != 0L || seconds != 0L) (
            if (minutes < 10) ".0$minutes" else ".$minutes") else ""
    }

    private fun getSecondsStr(seconds: Long): String {
        return if (seconds != 0L) (if (seconds < 10) ":0$seconds" else ":$seconds") else ""
    }

    private fun getReadableSchedule(weeklyScheduleList: MutableList<DayIntervalsDTO>): String {
        val respStrBuilder = StringBuilder("Restaurant is open:\n")
        for (dayIntervalsDTO in weeklyScheduleList) {
            respStrBuilder.append("${dayIntervalsDTO.day.name.capitalize()}: ")
            if (dayIntervalsDTO.intervalList.isEmpty()) {
                respStrBuilder.append("Closed")
            } else {
                for ((index, period) in dayIntervalsDTO.intervalList.withIndex()) {
                    respStrBuilder.append(period)
                    if (index < dayIntervalsDTO.intervalList.size - 1)
                        respStrBuilder.append(", ")
                }
            }
            respStrBuilder.append("\n")
        }
        getLogger().info("Restaurant's timetable has been converted to readable format")
        return respStrBuilder.toString()
    }
}