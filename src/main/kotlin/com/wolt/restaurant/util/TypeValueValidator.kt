package com.wolt.restaurant.util

import com.wolt.restaurant.dto.TypeValueDTO
import com.wolt.restaurant.exception.InaccurateTimingException
import com.wolt.restaurant.exception.NoSuchDayException
import com.wolt.restaurant.exception.NoSuchTypeException
import com.wolt.restaurant.util.Constants.EXP_MSG_NO_SUCH_DAY
import com.wolt.restaurant.util.Constants.EXP_MSG_NO_SUCH_TYPE
import com.wolt.restaurant.util.Constants.MAX_TIME_VALUE
import com.wolt.restaurant.util.Constants.MIN_TIME_VALUE
import com.wolt.restaurant.util.Constants.REASON_NO_SUCH_DAY
import com.wolt.restaurant.util.Constants.REASON_NO_SUCH_TYPE
import kotlin.collections.HashMap

class TypeValueValidator {

    fun validateTimetableInput(typeValueMap: HashMap<WeekDays, List<TypeValueDTO>>) {
        getLogger().info("Validate timetable input")
        checkWeekdayNames(typeValueMap.keys)
        checkDayIntervalsList(typeValueMap.values)
    }

    @Throws(NoSuchDayException::class)
    private fun checkWeekdayNames(daysMutableSet: MutableSet<WeekDays>) {
        val daysSet: Set<WeekDays> = daysMutableSet.toSet()
        daysSet.forEach { d->
            if(!WeekDays.values().contains(d)){
                val usableDays = WeekDays.values().contentToString()
                throw NoSuchDayException(EXP_MSG_NO_SUCH_DAY,
                    "$REASON_NO_SUCH_DAY $usableDays")
            }
        }
    }

    @Throws(NoSuchDayException::class, InaccurateTimingException::class)
    private fun checkDayIntervalsList(mutListOfObjList: MutableCollection<List<TypeValueDTO>>) {
        val daysIntervalsList: List<List<TypeValueDTO>> = mutListOfObjList.toList()

        daysIntervalsList.forEach{intervalList->
            for(typeValueObj in intervalList){
                checkTypeField(typeValueObj.type)
                checkValueField(typeValueObj.value)
            }
        }
    }

    private fun checkTypeField(type: String) {
        if(type != TypeEnum.open.name && type != TypeEnum.close.name){
            val usableTypes = TypeEnum.values().contentToString()
            throw NoSuchTypeException(EXP_MSG_NO_SUCH_TYPE,
                "$REASON_NO_SUCH_TYPE $usableTypes")
        }
    }

    private fun checkValueField(value: Int) {
        if(value < MIN_TIME_VALUE || value > MAX_TIME_VALUE)
            throw InaccurateTimingException(Constants.EXP_MSG_INACCURATE_TIMING,
                Constants.REASON_INACCURATE_TIMING_VALUE)
    }
}