package com.wolt.restaurant.exception

import com.wolt.restaurant.util.ErrorResponse
import org.springframework.http.ResponseEntity

class ResponseEntityBuilder {
    fun build(apiError: ErrorResponse): ResponseEntity<Any> {
        return ResponseEntity(apiError, apiError.status)
    }
}