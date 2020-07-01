package com.wolt.restaurant.exception

import com.wolt.restaurant.util.ErrorResponse
import com.wolt.restaurant.util.ResponseConstants
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.LocalDateTime

@ControllerAdvice
class EndpointControllerAdvice: ResponseEntityExceptionHandler() {

    @ExceptionHandler(UnmatchedOpenCloseTimeException::class)
    fun unmatchedOpenCloseTime(ex: UnmatchedOpenCloseTimeException): ResponseEntity<Any> {
        val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
            "Malformed JSON request", ex.message.toString())
        return ResponseEntityBuilder().build(errorResponse)
    }

    @ExceptionHandler(InaccurateTimingException::class)
    fun inaccurateTiming(ex: InaccurateTimingException): ResponseEntity<Any> {
        val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
            "Malformed JSON request", ex.message.toString())
        return ResponseEntityBuilder().build(errorResponse)
    }

    @ExceptionHandler(NoSuchDayException::class)
    fun noSuchDay(ex: NoSuchDayException): ResponseEntity<Any> {
        val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
            ResponseConstants.NO_SUCH_DAY.value, ex.message.toString())
        return ResponseEntityBuilder().build(errorResponse)
    }

    @ExceptionHandler(NoSuchTypeException::class)
    fun noSuchType(ex: NoSuchTypeException): ResponseEntity<Any> {
        val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
            ResponseConstants.NO_SUCH_TYPE.value, ex.message.toString())
        return ResponseEntityBuilder().build(errorResponse)
    }

    override fun handleNoHandlerFoundException(ex: NoHandlerFoundException,
            headers: HttpHeaders, status: HttpStatus,
            request: WebRequest): ResponseEntity<Any> {
        val errorResponse= ErrorResponse( LocalDateTime.now(),HttpStatus.NOT_FOUND,
            "No handler found for ${ex.httpMethod} ${ex.requestURL}",
            ex.message.toString())
        return ResponseEntityBuilder().build(errorResponse)
    }

    override fun handleHttpMessageNotReadable(ex:HttpMessageNotReadableException,
            headers: HttpHeaders, status: HttpStatus,
            request:WebRequest): ResponseEntity<Any> {
        val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
            "Malformed JSON request", ex.message.toString())
        return ResponseEntityBuilder().build(errorResponse)
    }

    override fun handleHttpMediaTypeNotSupported(ex:HttpMediaTypeNotSupportedException,
             headers: HttpHeaders,status: HttpStatus,
             request:WebRequest): ResponseEntity<Any> {
        val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "Unsupported media type", ex.message.toString())
        return ResponseEntityBuilder().build(errorResponse)
    }

    override fun handleHttpRequestMethodNotSupported(ex: HttpRequestMethodNotSupportedException,
             headers: HttpHeaders,status: HttpStatus,
             request:WebRequest): ResponseEntity<Any> {
        val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.METHOD_NOT_ALLOWED,
            "Unsupported request method", ex.message.toString())
        return ResponseEntityBuilder().build(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun defaultErrorHandler(ex: Exception, request: WebRequest) : ResponseEntity<Any>  {
        val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
            "An error occurred", ex.localizedMessage)
        return ResponseEntityBuilder().build(errorResponse)
    }
}