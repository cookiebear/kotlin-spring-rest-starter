package dev.starter.modules.auth

import dev.starter.common.api.ApiResponse
import dev.starter.common.error.ApiException
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.time.Instant


data class RegisterRequest(@field:Email @field:NotBlank val email: String, @field:Size(min = 8, max = 72) val password: String)
data class LoginRequest(@field:Email @field:NotBlank val email: String, @field:NotBlank val password: String)
data class UserDto(val id: Long, val email: String)
data class TokenDto(val accessToken: String, val tokenType: String = "Bearer", val expiresIn: Long)

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val users: UserRepository,
    private val encoder: PasswordEncoder,
    private val jwtEncoder: JwtEncoder,
    @Value("\${app.jwt.expiration-minutes}") private val expirationMinutes: Long
) {
    @PostMapping("/register")
    @Transactional
    fun register(@Valid @RequestBody body: RegisterRequest): ResponseEntity<ApiResponse<UserDto>> {
        val email = body.email.trim().lowercase()
        if (users.existsByEmail(email)) throw ApiException(HttpStatus.CONFLICT, "EMAIL_EXISTS", "Email already registered")
        val user = users.save(User(email = email, passwordHash = encoder.encode(body.password)))
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(UserDto(user.id!!, user.email)))
    }

    @PostMapping("/login")
    @Transactional(readOnly = true)
    fun login(@Valid @RequestBody body: LoginRequest): ApiResponse<TokenDto> {
        val user = users.findByEmail(body.email.trim().lowercase())
        if (user == null || !encoder.matches(body.password, user.passwordHash))
            throw ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid email or password")
        val now = Instant.now()
        val claims = JwtClaimsSet.builder().issuer("kotlin-spring-rest-starter").subject(user.id!!.toString())
            .issuedAt(now).expiresAt(now.plusSeconds(expirationMinutes * 60)).build()
        val token = jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).tokenValue
        return ApiResponse.ok(TokenDto(token, expiresIn = expirationMinutes * 60))
    }
}
