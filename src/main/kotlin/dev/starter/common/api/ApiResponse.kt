package dev.starter.common.api

data class ApiResponse<T>(val success: Boolean, val data: T? = null, val error: ApiError? = null, val traceId: String? = null) {
    companion object {
        fun <T> ok(data: T, traceId: String? = null) = ApiResponse(success = true, data = data, traceId = traceId)
        fun fail(error: ApiError, traceId: String? = null): ApiResponse<Nothing> = ApiResponse(success = false, error = error, traceId = traceId)
    }
}
data class ApiError(val code: String, val message: String, val details: List<FieldErrorDetail> = emptyList())
data class FieldErrorDetail(val field: String, val reason: String)
data class PageResponse<T>(val items: List<T>, val page: Int, val size: Int, val totalElements: Long, val totalPages: Int)
