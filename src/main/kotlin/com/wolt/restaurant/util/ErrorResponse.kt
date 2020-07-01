package com.wolt.restaurant.util

import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class ErrorResponse(
    val timestamp: LocalDateTime,
    val status: HttpStatus,
    val error: String,
    val message: String
    )