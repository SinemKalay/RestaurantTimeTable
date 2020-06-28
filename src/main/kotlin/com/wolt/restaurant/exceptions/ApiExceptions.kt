package com.wolt.restaurant.exceptions

class NoSuchDayException(faultyDay: String) :
    RuntimeException("There is no such day named as $faultyDay")

class NoSuchTypeException(faultyType: String) :
    RuntimeException("There is no such type named as $faultyType")

class UnmatchedOpenCloseTimeException(message: String) :
    RuntimeException(message)

class InaccurateTimingException(message: String) :
    RuntimeException(message)