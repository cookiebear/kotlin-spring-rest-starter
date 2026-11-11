package dev.starter.common.error

import org.springframework.http.HttpStatus

class ApiException(val status: HttpStatus, val code: String, override val message: String) : RuntimeException(message)
