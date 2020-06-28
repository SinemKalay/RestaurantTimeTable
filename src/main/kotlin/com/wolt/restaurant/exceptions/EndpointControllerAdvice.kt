package com.wolt.restaurant.exceptions

import com.wolt.restaurant.utils.ErrorResponse
import com.wolt.restaurant.utils.ResponseConstants
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class EndpointControllerAdvice {

    @ExceptionHandler(NoSuchDayException::class)
    fun noSuchDay(noSuchDayException: NoSuchDayException):
        ResponseEntity<ErrorResponse> {
        val res = ErrorResponse(ResponseConstants.NO_SUCH_DAY.value,
            noSuchDayException.message.toString())
        return ResponseEntity.badRequest().body(res)
    }

    @ExceptionHandler(NoSuchTypeException::class)
    fun noSuchType(noSuchTypeException: NoSuchTypeException):
        ResponseEntity<ErrorResponse> {
        val res = ErrorResponse(ResponseConstants.NO_SUCH_TYPE.value,
            noSuchTypeException.message.toString())
        return ResponseEntity.badRequest().body(res)
    }

    @ExceptionHandler(UnmatchedOpenCloseTimeException::class)
    fun unmatchedOpenCloseTime(unmatchedOpenCloseTimeException: UnmatchedOpenCloseTimeException):
        ResponseEntity<ErrorResponse> {
        val res = ErrorResponse(ResponseConstants.CORRUPTED_DATA.value,
            unmatchedOpenCloseTimeException.message.toString())
        return ResponseEntity.badRequest().body(res)
    }

    @ExceptionHandler(InaccurateTimingException::class)
    fun inaccurateTiming(inaccurateTimingException: InaccurateTimingException):
        ResponseEntity<ErrorResponse> {
        val res = ErrorResponse(ResponseConstants.INCOMP_DATA.value,
            inaccurateTimingException.message.toString())
        return ResponseEntity.badRequest().body(res)
    }

}