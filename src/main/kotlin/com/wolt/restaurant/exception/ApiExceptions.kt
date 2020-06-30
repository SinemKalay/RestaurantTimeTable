package com.wolt.restaurant.exception

import com.wolt.restaurant.util.getLogger

class NoSuchDayException(message: String):  RuntimeException(message){
    @JvmOverloads
    constructor(message: String, reason: String): this(message){
        getLogger().error("Request processing failed: " +
            "NoSuchTypeException: $message with reason 'There is no such day named as $reason'")
    }
}

class NoSuchTypeException(message: String):  RuntimeException(message){
    @JvmOverloads
    constructor(message: String, reason: String): this(message){
        getLogger().error("Request processing failed: " +
            "NoSuchTypeException: $message with reason 'There is no such day named as $reason'")
    }
}

class UnmatchedOpenCloseTimeException(message: String):  RuntimeException(message){
    @JvmOverloads
    constructor(message: String, reason: String): this(message){
        getLogger().error("Request processing failed: " +
            "UnmatchedOpenCloseTimeException: $message with reason '$reason'")
    }
}

class InaccurateTimingException(message: String) :  RuntimeException(message){
    @JvmOverloads constructor(message: String, reason: String): this(message){
        getLogger().error("Request processing failed: " +
            "InaccurateTimingException : $message with reason '$reason'")
    }
}