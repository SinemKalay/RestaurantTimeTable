package com.wolt.restaurant.util

import com.wolt.restaurant.dto.TypeValueDTO
import com.wolt.restaurant.exception.InaccurateTimingException
import com.wolt.restaurant.exception.NoSuchDayException
import com.wolt.restaurant.exception.NoSuchTypeException
import kotlin.collections.HashMap

class TypeValueValidator {

    fun isValidatedOk(typeValueMap: HashMap<WeekDays, List<TypeValueDTO>>): Boolean {
        return checkWeekdayNames(typeValueMap.keys) &&
            checkTypeValueDTOList(typeValueMap.values)
    }

    @Throws(NoSuchDayException::class)
    private fun checkWeekdayNames(daysMutableSet: MutableSet<WeekDays>): Boolean {
        val daysSet: Set<WeekDays> = daysMutableSet.toSet()
        daysSet.forEach { d->
            if(!WeekDays.values().contains(d)){
                var usableDays = WeekDays.values().contentToString()
                throw NoSuchDayException(Constants.EXP_MSG_NO_SUCH_DAY,
                    "${Constants.REASON_NO_SUCH_DAY} $usableDays")
            }
        }
        return true
    }

    @Throws(NoSuchDayException::class, InaccurateTimingException::class)
    private fun checkTypeValueDTOList(mutListOfObjList: MutableCollection<List<TypeValueDTO>>): Boolean {
        val outerList: List<List<TypeValueDTO>> = mutListOfObjList.toList()

        outerList.forEach{innerList->
            for(typeValueObj in innerList){
                checkTypeField(typeValueObj.type)
                checkValueField(typeValueObj.value)
            }
        }
        return true
    }

    private fun checkTypeField(type: TypeEnum) {
        if(!TypeEnum.values().contains(type)){
            var usableTypes = TypeEnum.values().contentToString()
            throw NoSuchTypeException(Constants.EXP_MSG_NO_SUCH_TYPE,
                "${Constants.REASON_NO_SUCH_TYPE} $usableTypes")
        }
    }

    private fun checkValueField(value: Int) {
        if(value < 0 || value > 86399)
            throw InaccurateTimingException(Constants.EXP_MSG_INACCURATE_TIMING,
                Constants.REASON_INACCURATE_TIMING_VALUE)
    }
}