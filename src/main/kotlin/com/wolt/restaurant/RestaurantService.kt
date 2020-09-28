package com.wolt.restaurant

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
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

   // TODO: Correct output of exact hours
   // TODO: Refactor unit tests

    fun processAlgorithmThenReturnResponse(inputJsonStr: String): String{
        val mapDayToHoursList = validateInputAndConvertToHashMap(inputJsonStr)
        val weeklyScheduleList = processDays(mapDayToHoursList)

        return createReadableResponse(weeklyScheduleList)
    }
    fun validateInputAndConvertToHashMap(scheduleJsonStr: String): HashMap<WeekDays, List<TypeValueDTO>> {
        getLogger().info("PArse json input to TypeValueDTO")
        val mapDayToHoursList = parseJsonToDTO(scheduleJsonStr)
        getLogger().info("Restaurant working time data is sent to validator")
        TypeValueValidator().validateScheduleInput(mapDayToHoursList)

        return mapDayToHoursList
    }

    private fun processDays(mapDayToHoursList: HashMap<WeekDays,List<TypeValueDTO>>)
            : MutableList<WorkingTimesForADayDTO>{
        var weeklyScheduleList: MutableList<WorkingTimesForADayDTO> = mutableListOf()
        val daysInInputString = WeekDays.values().filter { d -> mapDayToHoursList.containsKey(d) }
        val mapDayToSortedHoursList = sortAllTimeValues(mapDayToHoursList, daysInInputString)
        for (currentDay in daysInInputString.reversed()) {
            val hoursList = mapDayToHoursList.getValue(currentDay)
            if (hoursList.isEmpty()) {
                weeklyScheduleList.add(markDayAsClosed(currentDay))
            } else {
                val listIntervalsOfCurrentDay =
                    checkOtherDaysRestaurantWorking(currentDay, mapDayToSortedHoursList)
                val workingTimesForADay = WorkingTimesForADayDTO(currentDay, listIntervalsOfCurrentDay)
                weeklyScheduleList.add(workingTimesForADay)
            }
        }
        getLogger("Schedule analyze operation has been finished")
        return weeklyScheduleList.reversed().toMutableList()
    }

    private fun sortAllTimeValues(dayToListMap: HashMap<WeekDays, List<TypeValueDTO>>,
            daysInInputString: List<WeekDays>): HashMap<WeekDays,List<TypeValueDTO>> {
        for (currentDay in daysInInputString) {
            if (!dayToListMap[currentDay].isNullOrEmpty()){
                val sortedHoursList = dayToListMap[currentDay]!!.sortedBy { it.value }
                dayToListMap[currentDay] = sortedHoursList
            }
        }
        return dayToListMap
    }

    private fun checkOtherDaysRestaurantWorking(currentDay: WeekDays,
            mapDayToSortedHoursListParam: HashMap<WeekDays, List<TypeValueDTO>>): MutableList<String> {
        var openingTimeSeconds = -1
        var workingTimesList:MutableList<String> = mutableListOf()
        var mapDayToSortedHoursList = mapDayToSortedHoursListParam

        getLogger("Look intervals of $currentDay")
        for ((index, typeValueDTO) in mapDayToSortedHoursList[currentDay]!!.withIndex()) {
            when (typeValueDTO.type) {
                TypeEnum.open.name -> {
                    val(workingTimesListTemp, openingTimeSecondsTemp)=
                        whenTypeIsOpen(currentDay,
                            mapDayToSortedHoursList,typeValueDTO,workingTimesList,openingTimeSeconds)
                    workingTimesList = workingTimesListTemp
                    openingTimeSeconds = openingTimeSecondsTemp
                }
                TypeEnum.close.name -> {
                    val(mapDayToSortedHoursListTemp,
                        workingTimesListTemp, openingTimeSecondsTemp)=
                        whenTypeIsClose(currentDay, mapDayToSortedHoursList,
                            typeValueDTO,workingTimesList,openingTimeSeconds)
                    mapDayToSortedHoursList = mapDayToSortedHoursListTemp
                    workingTimesList = workingTimesListTemp
                    openingTimeSeconds = openingTimeSecondsTemp
                }
            }
        }
        getLogger("Intervals of $currentDay checked")
        return workingTimesList
    }

    private fun whenTypeIsOpen(currentDay: WeekDays,mapDayToSortedHoursList: HashMap<WeekDays, List<TypeValueDTO>>,
               typeValueDTO: TypeValueDTO, workingTimesList: MutableList<String>,
               openingTimeSeconds: Int): Pair<MutableList<String>, Int> {
        var openingTimeSecondsTemp = openingTimeSeconds
        if (typeValueDTO == mapDayToSortedHoursList[currentDay]!!.last()
            && hasNextDayClosingTimeInfo(mapDayToSortedHoursList, currentDay)) {
            val nextDay = getNextDay(currentDay)
            val closingTimeInfoOnNextDay = mapDayToSortedHoursList[nextDay]!!.first()
            workingTimesList.add(returnInterval(typeValueDTO.value,closingTimeInfoOnNextDay.value,
                true))
            openingTimeSecondsTemp=-1
        } else {
            if(openingTimeSecondsTemp != -1)
                throw UnmatchedOpenCloseTimeException(Constants.EXP_MSG_UNEXP_OPENING,
                    "Unexpected opening time on $currentDay")
            openingTimeSecondsTemp = typeValueDTO.value
        }
        return Pair(workingTimesList, openingTimeSecondsTemp)
    }

    private fun whenTypeIsClose(currentDay: WeekDays, mapDayToSortedHoursList: HashMap<WeekDays, List<TypeValueDTO>>,
                   typeValueDTO: TypeValueDTO, workingTimesList: MutableList<String>,
                   openingTimeSeconds: Int):
                    Triple<HashMap<WeekDays, List<TypeValueDTO>>,MutableList<String>, Int> {
        var openingTimeSecondsTemp = openingTimeSeconds
        if (typeValueDTO == mapDayToSortedHoursList[currentDay]!!.first()
            && hasPreviousDayClosingTimeInfoOnCurrentDay(mapDayToSortedHoursList, currentDay)) {
            val prevDay = getPreviousDay(currentDay)
            var tempList = mapDayToSortedHoursList[prevDay]!!.toMutableList()
            typeValueDTO.isOvertime = true
            tempList.add(typeValueDTO)
            mapDayToSortedHoursList[prevDay] = tempList
            tempList = mapDayToSortedHoursList[currentDay]!!.toMutableList()
            tempList.removeAt(0)
            mapDayToSortedHoursList[currentDay] = tempList
        } else {
            if (openingTimeSecondsTemp == -1)
                throw UnmatchedOpenCloseTimeException(Constants.EXP_MSG_UNEXP_CLOSING,
                    "Closing time information was not expected on $currentDay")
            workingTimesList.add(returnInterval(openingTimeSecondsTemp,typeValueDTO.value,
                typeValueDTO.isOvertime))
            openingTimeSecondsTemp=-1
        }
        return Triple(mapDayToSortedHoursList,workingTimesList, openingTimeSecondsTemp)
    }

    @Throws(InaccurateTimingException::class)
    private fun returnInterval(openingTimeSecond: Int, closingTimeSecond: Int, isOvertime: Boolean): String {
        if(!isOvertime && openingTimeSecond > closingTimeSecond)
            throw InaccurateTimingException(Constants.EXP_MSG_INACCURATE_TIMING,
                "Opening time can not be later than closing time")
        // TODO: since time values are being sorted at the begining this
        // scenario is eliminated. It will give another type of error

        val openingTime = convertTimeFormat(openingTimeSecond) + " - "
        val closingTime = convertTimeFormat(closingTimeSecond)
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
    private fun hasPreviousDayClosingTimeInfoOnCurrentDay(mapDayToSortedHoursList: HashMap<WeekDays, List<TypeValueDTO>>,
                                                          currentDay: WeekDays): Boolean {
        val previousDay = getPreviousDay(currentDay)
        if (mapDayToSortedHoursList.containsKey(previousDay)
            && mapDayToSortedHoursList[previousDay]!!.isNotEmpty()){
            if(mapDayToSortedHoursList[previousDay]!!.last().type != TypeEnum.open.toString())
                throw UnmatchedOpenCloseTimeException(Constants.EXP_MSG_UNEXP_CLOSING,
                    "Unexpected closing time on $currentDay")
        } else {
            throw UnmatchedOpenCloseTimeException(Constants.EXP_MSG_NON_SEQUENTIAL,
                "Opening-Closing times must be on same or sequential day")
        }
        return true
    }

    @Throws(UnmatchedOpenCloseTimeException::class)
    private fun hasNextDayClosingTimeInfo(mapDayToSortedHoursList: HashMap<WeekDays, List<TypeValueDTO>>,
                                          currentDay: WeekDays): Boolean {
        val nextDay = getNextDay(currentDay)
        if (mapDayToSortedHoursList.containsKey(nextDay)
            && mapDayToSortedHoursList[nextDay]!!.isNotEmpty()){
            if(mapDayToSortedHoursList[nextDay]!!.first().type != TypeEnum.close.toString())
                throw UnmatchedOpenCloseTimeException(Constants.EXP_MSG_UNEXP_OPENING,
                    "Unexpected opening time on $currentDay")
        } else {
            throw UnmatchedOpenCloseTimeException(Constants.EXP_MSG_UNEXP_OPENING,
                "Unclosed day exists $currentDay")
        }
        return true
    }

    private fun markDayAsClosed(day: WeekDays):WorkingTimesForADayDTO {
        val workingTimesForADay = WorkingTimesForADayDTO(day, listOf(Constants.ALL_DAY_CLOSED))
        getLogger().info("Restaurant has been marked all day closed on $day")
        return workingTimesForADay
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
        val timeStr = df.format(Date(sec * 1000L))
        getLogger().info("$sec is converted into $timeStr")
        return timeStr
    }

    private fun createReadableResponse(weeklyScheduleList: MutableList<WorkingTimesForADayDTO>): String {
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