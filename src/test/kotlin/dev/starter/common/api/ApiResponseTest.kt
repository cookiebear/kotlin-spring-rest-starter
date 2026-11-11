package dev.starter.common.api

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ApiResponseTest {
    @Test fun `success response wraps data`() {
        val result = ApiResponse.ok("hello")
        assertTrue(result.success)
        assertEquals("hello", result.data)
        assertNull(result.error)
    }
    @Test fun `error response has code`() {
        val result = ApiResponse.fail(ApiError("NOT_FOUND", "Not found"))
        assertFalse(result.success)
        assertEquals("NOT_FOUND", result.error?.code)
    }
}
