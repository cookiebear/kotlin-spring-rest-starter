package dev.starter.modules.notes

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface NoteRepository : JpaRepository<Note, Long> {
    fun findAllByOwnerId(ownerId: Long, pageable: Pageable): Page<Note>
    fun findByIdAndOwnerId(id: Long, ownerId: Long): Note?
}
