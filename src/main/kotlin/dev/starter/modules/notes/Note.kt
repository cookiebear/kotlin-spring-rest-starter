package dev.starter.modules.notes

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "notes")
class Note(
    @Column(name = "owner_id", nullable = false) val ownerId: Long,
    @Column(nullable = false, length = 150) var title: String,
    @Column(nullable = false, columnDefinition = "text") var content: String,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false) var createdAt: OffsetDateTime? = null,
    @Column(name = "updated_at", nullable = false, insertable = false) var updatedAt: OffsetDateTime? = null
) {
    @PreUpdate fun onUpdate() { updatedAt = OffsetDateTime.now() }
}
