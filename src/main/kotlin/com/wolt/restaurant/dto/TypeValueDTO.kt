package com.wolt.restaurant.dto

import com.google.gson.annotations.SerializedName
import com.wolt.restaurant.exception.TimeValueNotFoundException
import com.wolt.restaurant.exception.TypeNotFoundException
import com.wolt.restaurant.util.Constants

data class TypeValueDTO(
    @SerializedName("type") private val _type: String?,
    @SerializedName("value") private val _value: Int?

)
{
    val type
        get() = _type ?: throw TypeNotFoundException(
            Constants.EXP_MSG_TYPE_NOT_FOUND, Constants.REASON_TYPE_NOT_FOUND
        )

    val value
        get() = _value ?: throw TimeValueNotFoundException(
            Constants.EXP_MSG_VALUE_NOT_FOUND, Constants.REASON_VALUE_NOT_FOUND
        )

    init
    {
        this.type
        this.value
    }
}