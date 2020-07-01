package com.wolt.restaurant.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.wolt.restaurant.util.TypeEnum
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotEmpty

data class TypeValueDTO(
    @field:NotEmpty
    @JsonProperty("type")
    var type: TypeEnum,

    @JsonProperty("value")
    @field:[NotEmpty Min(0) Max(86399)]
    var value: Int
)


