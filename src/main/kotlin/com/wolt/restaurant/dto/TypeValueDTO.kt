package com.wolt.restaurant.dto

data class TypeValueDTO (
        var type: Type,
        var value: Int
){

    enum class Type{
        open, close
    }
}

