package com.wolt.restaurant.dto

import com.wolt.restaurant.util.Type
import org.jetbrains.annotations.NotNull
import javax.validation.constraints.Max
import javax.validation.constraints.Min

data class TypeValueDTO(
    @get:NotNull
    var type: Type,

    @Min(0)
    @Max(86399)
    var value: Int
)

