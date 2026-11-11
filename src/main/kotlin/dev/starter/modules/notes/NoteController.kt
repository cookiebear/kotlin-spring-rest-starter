package dev.starter.modules.notes

import dev.starter.common.api.ApiResponse
import dev.starter.common.api.PageResponse
import dev.starter.common.error.ApiException
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime

data class NoteRequest(@field:NotBlank @field:Size(max = 150) val title: String, @field:NotBlank val content: String)
data class NoteDto(val id: Long, val title: String, val content: String, val createdAt: OffsetDateTime?, val updatedAt: OffsetDateTime?)

@RestController
@RequestMapping("/api/v1/notes")
class NoteController(private val notes: NoteRepository) {
    private fun Note.dto() = NoteDto(id!!, title, content, createdAt, updatedAt)
    private fun owned(id: Long, owner: Long): Note = notes.findByIdAndOwnerId(id, owner)
        ?: throw ApiException(HttpStatus.NOT_FOUND, "NOTE_NOT_FOUND", "Note not found")

    @GetMapping
    @Transactional(readOnly = true)
    fun list(@AuthenticationPrincipal jwt: Jwt, @RequestParam(defaultValue = "0") page: Int,
             @RequestParam(defaultValue = "20") size: Int): ApiResponse<PageResponse<NoteDto>> {
        if (page < 0 || size !in 1..100) throw ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAGING", "page must be >= 0 and size must be 1..100")
        val result = notes.findAllByOwnerId(jwt.subject.toLong(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))
        return ApiResponse.ok(PageResponse(result.content.map { it.dto() }, page, size, result.totalElements, result.totalPages))
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    fun get(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: Long) = ApiResponse.ok(owned(id, jwt.subject.toLong()).dto())

    @PostMapping
    @Transactional
    fun create(@AuthenticationPrincipal jwt: Jwt, @Valid @RequestBody body: NoteRequest): ResponseEntity<ApiResponse<NoteDto>> {
        val note = notes.saveAndFlush(Note(jwt.subject.toLong(), body.title.trim(), body.content))
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(note.dto()))
    }

    @PutMapping("/{id}")
    @Transactional
    fun update(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: Long, @Valid @RequestBody body: NoteRequest): ApiResponse<NoteDto> {
        val note = owned(id, jwt.subject.toLong())
        note.title = body.title.trim()
        note.content = body.content
        return ApiResponse.ok(notes.saveAndFlush(note).dto())
    }

    @DeleteMapping("/{id}")
    @Transactional
    fun delete(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: Long): ResponseEntity<Void> {
        notes.delete(owned(id, jwt.subject.toLong()))
        return ResponseEntity.noContent().build()
    }
}
