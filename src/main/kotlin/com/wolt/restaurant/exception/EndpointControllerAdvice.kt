package com.wolt.restaurant.exception

import com.google.gson.JsonParseException
import com.wolt.restaurant.util.ErrorResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.LocalDateTime
import javax.servlet.http.HttpServletRequest

@ControllerAdvice
class EndpointControllerAdvice: ResponseEntityExceptionHandler() {

    @ExceptionHandler(UnmatchedOpenCloseTimeException::class)
    fun handleUnmatchedOpenCloseTimeException(ex: UnmatchedOpenCloseTimeException): ResponseEntity<Any> {
        val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
            "Malformed JSON request", ex.message.toString())
        return ResponseEntityBuilder().build(errorResponse)
    }

    @ExceptionHandler(InaccurateTimingException::class)
    fun handleInaccurateTimingException(ex: InaccurateTimingException): ResponseEntity<Any> {
        val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
            "Malformed JSON request", ex.message.toString())
        return ResponseEntityBuilder().build(errorResponse)
    }

    @ExceptionHandler(NoSuchDayException::class)
    fun handleNoSuchDayException(ex: NoSuchDayException): ResponseEntity<Any> {
        val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
            "Malformed JSON request", ex.message.toString())
        return ResponseEntityBuilder().build(errorResponse)
    }

    @ExceptionHandler(TypeNotFoundException::class)
    fun handleTypeNotFoundException(ex: TypeNotFoundException): ResponseEntity<Any> {
        val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.NOT_FOUND,
            "Malformed JSON request", ex.message.toString())
        return ResponseEntityBuilder().build(errorResponse)
    }

    @ExceptionHandler(TimeValueNotFoundException::class)
    fun handleTimeValueNotFoundException(ex: TimeValueNotFoundException): ResponseEntity<Any> {
        val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.NOT_FOUND,
            "Malformed JSON request", ex.message.toString())
        return ResponseEntityBuilder().build(errorResponse)
    }

    @ExceptionHandler(NoSuchTypeException::class)
    fun handleNoSuchTypeException(ex: NoSuchTypeException): ResponseEntity<Any> {
        val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
            "Malformed JSON request", ex.message.toString())
        return ResponseEntityBuilder().build(errorResponse)
    }

    @ExceptionHandler(JsonParseException::class)
    fun handleJsonParseException(ex: JsonParseException, req : HttpServletRequest): ResponseEntity<Any> {
        val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
            "Malformed JSON request", ex.message.toString())
        return ResponseEntityBuilder().build(errorResponse)
    }

    @ExceptionHandler(NumberFormatException::class)
    fun handleNumberFormatException(ex: NumberFormatException, req : HttpServletRequest): ResponseEntity<Any> {
        val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
            "Malformed JSON request", ex.message.toString())
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