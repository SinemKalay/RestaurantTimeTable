package com.wolt.restaurant

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.wolt.restaurant.dto.TypeValueDTO
import com.wolt.restaurant.exception.RespBodyNotFoundException
import com.wolt.restaurant.exception.UnmatchedOpenCloseTimeException
import com.wolt.restaurant.util.Constants
import com.wolt.restaurant.util.Constants.MAX_TIME_VALUE
import com.wolt.restaurant.util.TypeEnum
import com.wolt.restaurant.util.TypeValueValidator
import com.wolt.restaurant.util.WeekDays
import com.wolt.restaurant.util.getLogger
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

@Service
class RestaurantService
{

    fun formatThenReturnTimetable(timetableJsonStr: String): String
    {
        val mapDayIntervals = convertToHashMapAndValidate(timetableJsonStr)
        val mapWeeklySchedule = getWeeklySchedule(mapDayIntervals)

        return getReadableSchedule(mapWeeklySchedule)
    }

    fun convertToHashMapAndValidate(timetableJsonStr: String): HashMap<WeekDays, List<TypeValueDTO>>
    {
        val mapDayIntervalList = parseJsonToDTO(timetableJsonStr)
        getLogger().info("Json input parsed into HashMap<WeekDays, List<TypeValueDTO>>")
        TypeValueValidator().validateTimetableInput(mapDayIntervalList)
        getLogger().info("HashMap validated")

        return mapDayIntervalList
    }

    private fun getWeeklySchedule(mapDayIntervalList: HashMap<WeekDays, List<TypeValueDTO>>):
        Map<WeekDays, Set<String>>
    {
        val dayIntervalPairs: MutableList<Pair<WeekDays, String>> = mutableListOf()
        val wholeWeekTypeValueList = getTypeValueDTOList(mapDayIntervalList)

        if (isOpenCloseTypeNumbersMatching(wholeWeekTypeValueList)
            && isOpenCloseTimesSequential(wholeWeekTypeValueList)
        )
        {
            val closedDays = mapDayIntervalList.filter {
                it.value.isEmpty() ||
                    (it.value.size == 1 && it.value[0].type == TypeEnum.close.name)
            }.keys
            closedDays.stream().forEach { dayIntervalPairs.add(Pair(it, "Closed")) }

            if (closedDays.size != mapDayIntervalList.size)
            {
                dayIntervalPairs.addAll(getDayIntervalPairs(wholeWeekTypeValueList))
            }
        }
        getLogger("Schedule analyze operation has been finished")

        return groupPairsToMapByWeekdays(dayIntervalPairs)
    }

    @Throws(UnmatchedOpenCloseTimeException::class)
    private fun getDayIntervalPairs(
        wholeWeekTypeValueList: MutableList<TypeValueDTO>
    ): MutableList<Pair<WeekDays, String>>
    {
        val dayIntervalPairs: MutableList<Pair<WeekDays, String>> = mutableListOf()
        val isSundayOvernightWorkExist = isSundayOvernightWorkExist(wholeWeekTypeValueList)
        val firstIndex: Int = if (isSundayOvernightWorkExist) 1 else 0
        val lastIndex: Int = if (isSundayOvernightWorkExist)
            wholeWeekTypeValueList.size - 1 else wholeWeekTypeValueList.size

        for (i in firstIndex until lastIndex)
        {
            if (wholeWeekTypeValueList[i].type == TypeEnum.open.name)
            {
                val openingDayIndex: Int = wholeWeekTypeValueList[i].value / MAX_TIME_VALUE
                val closingDayIndex: Int = wholeWeekTypeValueList[i + 1].value / MAX_TIME_VALUE

                if (closingDayIndex - openingDayIndex > 1)
                {
                    throw UnmatchedOpenCloseTimeException(
                        Constants.EXP_MSG_NON_SEQUENTIAL,
                        "Opening-Closing times must be on same or sequential day"
                    )
                }
                val interval = returnInterval(
                    getTimeInSeconds(wholeWeekTypeValueList[i]),
                    getTimeInSeconds(wholeWeekTypeValueList[i + 1])
                )
                dayIntervalPairs.add(Pair(WeekDays.values()[openingDayIndex], interval))
            }
        }
        if (isSundayOvernightWorkExist)
        {
            val interval = returnInterval(
                getTimeInSeconds(wholeWeekTypeValueList.last()),
                getTimeInSeconds(wholeWeekTypeValueList.first())
            )
            dayIntervalPairs.add(Pair(WeekDays.sunday, interval))
        }
        return dayIntervalPairs
    }

    private fun getTimeInSeconds(typeValueDTO: TypeValueDTO): Int
    {
        return typeValueDTO.value % MAX_TIME_VALUE
    }

    private fun getTypeValueDTOList(
        mapDaySortedHoursListParam: HashMap<WeekDays, List<TypeValueDTO>>
    ): MutableList<TypeValueDTO>
    {
        val wholeWeekTypeValueList: MutableList<TypeValueDTO> = mutableListOf()

        for ((day, typeValueDTOList) in mapDaySortedHoursListParam)
        {
            for (typeValueDTO in typeValueDTOList)
            {
                val value = day.ordinal * MAX_TIME_VALUE + typeValueDTO.value
                wholeWeekTypeValueList.add(TypeValueDTO(typeValueDTO.type, value))
            }
        }
        return wholeWeekTypeValueList.sortedBy { it.value }.toMutableList()
    }

    private fun groupPairsToMapByWeekdays(dayIntervalPairs: MutableList<Pair<WeekDays, String>>):
        Map<WeekDays, Set<String>>
    {
        return dayIntervalPairs.groupBy { it.first }
            .mapValues { it.value.map { p -> p.second }.toSet() }
            .toSortedMap(compareBy<WeekDays> { it.ordinal })
    }

    @Throws(UnmatchedOpenCloseTimeException::class)
    private fun isSundayOvernightWorkExist(wholeWeekTypeValueList: MutableList<TypeValueDTO>): Boolean
    {
        var sundayOvernight = false

        if (wholeWeekTypeValueList.first().type == TypeEnum.close.name &&
            wholeWeekTypeValueList.last().type == TypeEnum.open.name
        )
        {
            val firstDayInTimetable: Int = wholeWeekTypeValueList.first().value / MAX_TIME_VALUE
            val lastDayInTimetable: Int = wholeWeekTypeValueList.last().value / MAX_TIME_VALUE
            if (!(firstDayInTimetable == 0 && lastDayInTimetable == 6))
            {
                throw UnmatchedOpenCloseTimeException(
                    Constants.EXP_MSG_NON_SEQUENTIAL,
                    "Opening-Closing times must be on same or sequential day"
                )
            }
            sundayOvernight = true
        }
        return sundayOvernight
    }

    @Throws(UnmatchedOpenCloseTimeException::class)
    private fun isOpenCloseTypeNumbersMatching(wholeWeekTypeValueList: MutableList<TypeValueDTO>): Boolean
    {
        val numberOfOpenType: Int = wholeWeekTypeValueList.filter { it.type == TypeEnum.open.name }.count()
        val numberOfCloseType: Int = wholeWeekTypeValueList.filter { it.type == TypeEnum.close.name }.count()

        if (numberOfOpenType != numberOfCloseType)
            throw UnmatchedOpenCloseTimeException(
                Constants.EXP_MSG_UNMATCHED_OPEN_CLOSE,
                "Please check # of open/close times"
            )
        return true
    }

    @Throws(UnmatchedOpenCloseTimeException::class)
    private fun isOpenCloseTimesSequential(wholeWeekTypeValueList: MutableList<TypeValueDTO>): Boolean
    {
        for (i in 1 until wholeWeekTypeValueList.size)
        {
            if (wholeWeekTypeValueList[i - 1].type == wholeWeekTypeValueList[i].type)
            {
                val currentDayIndex: Int = wholeWeekTypeValueList[i].value / MAX_TIME_VALUE
                val currentDay: String = WeekDays.values()[currentDayIndex].toString()
                when (wholeWeekTypeValueList[i].type)
                {
                    TypeEnum.open.name ->
                        throw UnmatchedOpenCloseTimeException(
                            Constants.EXP_MSG_UNEXP_OPENING,
                            "Unexpected opening time on $currentDay"
                        )
                    TypeEnum.close.name ->
                        throw UnmatchedOpenCloseTimeException(
                            Constants.EXP_MSG_UNEXP_CLOSING,
                            "Unexpected closing time on $currentDay"
                        )
                }
            }
        }
        return true
    }

    private fun returnInterval(openingTimeSecond: Int, closingTimeSecond: Int): String
    {
        val openingTime = convertTimeFormat(openingTimeSecond.toLong()) + " - "
        val closingTime = convertTimeFormat(closingTimeSecond.toLong())
        return "$openingTime$closingTime"
    }

    @Throws(JsonSyntaxException::class, RespBodyNotFoundException::class)
    private fun parseJsonToDTO(jsonStr: String): HashMap<WeekDays, List<TypeValueDTO>>
    {
        val gson = Gson()
        val convertedObj: HashMap<WeekDays, List<TypeValueDTO>> =
            gson.fromJson(jsonStr, object : TypeToken<HashMap<WeekDays, List<TypeValueDTO>>>()
            {}.type)
        if (convertedObj.size == 0)
            throw RespBodyNotFoundException(
                Constants.EXP_MSG_RESP_BODY_NOT_FOUND,
                Constants.REASON_RESP_BODY_NOT_FOUND
            )
        getLogger().info("Json string converted to HashMap successfully")

        return convertedObj
    }

    private fun convertTimeFormat(sec: Long): String
    {
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

    private fun getHourStr(hours: Long): String
    {
        return when
        {
            hours == 0L -> "12"
            hours > 12L -> (hours % 12).toString()
            else -> hours.toString()
        }
    }

    private fun getMinuteStr(minutes: Long, seconds: Long): String
    {
        return if (minutes != 0L || seconds != 0L) (
            if (minutes < 10) ".0$minutes" else ".$minutes") else ""
    }

    private fun getSecondsStr(seconds: Long): String
    {
        return if (seconds != 0L) (if (seconds < 10) ":0$seconds" else ":$seconds") else ""
    }

    private fun getReadableSchedule(weeklyScheduleList: Map<WeekDays, Set<String>>): String
    {
        val respStrBuilder = StringBuilder("Restaurant is open:\n")
        for ((weekday, intervalList) in weeklyScheduleList)
        {
            respStrBuilder.append("${weekday.name.capitalize()}: ")
            if (intervalList.first() == "Closed")
            {
                respStrBuilder.append("Closed")
            } else
            {
                for ((index, period) in intervalList.withIndex())
                {
                    respStrBuilder.append(period)
                    if (index < intervalList.size - 1)
                        respStrBuilder.append(", ")
                }
            }
            respStrBuilder.append("\n")
        }
        getLogger().info("Restaurant's timetable has been converted to readable format")
        return respStrBuilder.toString()
    }
}