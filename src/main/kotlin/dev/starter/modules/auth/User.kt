package dev.starter.modules.auth

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "app_users")
class User(
    @Column(nullable = false, unique = true) var email: String,
    @Column(name = "password_hash", nullable = false) var passwordHash: String,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false) var createdAt: OffsetDateTime? = null
)
