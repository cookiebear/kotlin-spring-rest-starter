package dev.starter.common.error

import dev.starter.common.api.ApiError
import dev.starter.common.api.ApiResponse
import dev.starter.common.api.FieldErrorDetail
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)
    private fun response(status: HttpStatus, code: String, message: String, request: HttpServletRequest, details: List<FieldErrorDetail> = emptyList()): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(status).body(ApiResponse.fail(ApiError(code, message, details), request.getAttribute("traceId") as? String))

    @ExceptionHandler(ApiException::class)
    fun api(e: ApiException, req: HttpServletRequest) = response(e.status, e.code, e.message, req)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validation(e: MethodArgumentNotValidException, req: HttpServletRequest) = response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Invalid request fields", req,
        e.bindingResult.fieldErrors.map { FieldErrorDetail(it.field, it.defaultMessage ?: "Invalid") })

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun malformed(e: HttpMessageNotReadableException, req: HttpServletRequest) = response(HttpStatus.BAD_REQUEST, "INVALID_JSON", "Malformed request body", req)

    @ExceptionHandler(AccessDeniedException::class)
    fun forbidden(e: AccessDeniedException, req: HttpServletRequest) = response(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied", req)

    @ExceptionHandler(Exception::class)
    fun unexpected(e: Exception, req: HttpServletRequest): ResponseEntity<ApiResponse<Nothing>> {
        log.error("Unexpected error", e)
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected server error", req)
    }
}
