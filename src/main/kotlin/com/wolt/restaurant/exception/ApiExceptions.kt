package com.wolt.restaurant.exception

import com.wolt.restaurant.util.getLogger

class NoSuchDayException(message: String):  RuntimeException(message){
    constructor(message: String, reason: String): this("$message $reason"){
        getLogger().error("Request processing failed: " +
                "NoSuchDayException: $message with reason 'There is no such day named as $reason'")
    }
}

class RespBodyNotFoundException(message: String):  RuntimeException(message){
    constructor(message: String, reason: String): this("$message $reason"){
        getLogger().error("Request processing failed: " +
                "ResponseBodyNotFoundException: $message with reason '$reason'")
    }
}

class NoSuchTypeException(message: String):  RuntimeException(message){
    constructor(message: String, reason: String):  this("$message $reason"){
        getLogger().error("Request processing failed: " +
                "NoSuchTypeException: $message with reason 'There is no such day named as $reason'")
    }
}

class TimeValueNotFoundException(message: String):  RuntimeException(message){
    constructor(message: String, reason: String):  this("$message $reason"){
        getLogger().error("Request processing failed: " +
                "TimeValueNotFoundException: $message with reason '$reason'")
    }
}

class TypeNotFoundException(message: String):  RuntimeException(message){
    constructor(message: String, reason: String):  this("$message $reason"){
        getLogger().error("Request processing failed: " +
                "TypeNotFoundException: $message with reason '$reason'")
    }
}

class UnmatchedOpenCloseTimeException(message: String):  RuntimeException(message){
    constructor(message: String, reason: String):  this("$message $reason"){
        getLogger().error("Request processing failed: " +
                "UnmatchedOpenCloseTimeException: $message with reason '$reason'")
    }
}

class InaccurateTimingException(message: String):  RuntimeException(message){
    constructor(message: String, reason: String): this("$message $reason"){
        getLogger().error("Request processing failed: " +
                "InaccurateTimingException : $message with reason '$reason'")
    }
}