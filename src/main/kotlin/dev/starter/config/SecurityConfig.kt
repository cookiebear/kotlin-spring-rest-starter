package dev.starter.config

import com.nimbusds.jose.jwk.source.ImmutableSecret
import com.nimbusds.jose.proc.SecurityContext
import dev.starter.common.api.ApiError
import dev.starter.common.api.ApiResponse
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.web.SecurityFilterChain
import javax.crypto.spec.SecretKeySpec

@Configuration
class SecurityConfig {
    @Bean fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean fun jwtEncoder(@Value("\${app.jwt.secret}") secret: String): JwtEncoder {
        require(secret.toByteArray(Charsets.UTF_8).size >= 32) { "JWT secret must be at least 32 bytes" }
        return NimbusJwtEncoder(ImmutableSecret<SecurityContext>(secret.toByteArray(Charsets.UTF_8)))
    }

    @Bean fun jwtDecoder(@Value("\${app.jwt.secret}") secret: String): JwtDecoder {
        val key = SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256")
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build()
    }

    @Bean fun filterChain(http: HttpSecurity, mapper: ObjectMapper): SecurityFilterChain {
        http.csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login").permitAll()
                    .requestMatchers("/actuator/health", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { resource -> resource.jwt { } }
            .exceptionHandling { errors ->
                errors.authenticationEntryPoint { request, response, _ ->
                    response.status = HttpServletResponse.SC_UNAUTHORIZED
                    response.contentType = "application/json"
                    response.characterEncoding = "UTF-8"
                    mapper.writeValue(response.writer, ApiResponse.fail(ApiError("UNAUTHORIZED", "Authentication required"), request.getAttribute("traceId") as? String))
                }
                errors.accessDeniedHandler { request, response, _ ->
                    response.status = HttpServletResponse.SC_FORBIDDEN
                    response.contentType = "application/json"
                    response.characterEncoding = "UTF-8"
                    mapper.writeValue(response.writer, ApiResponse.fail(ApiError("FORBIDDEN", "Access denied"), request.getAttribute("traceId") as? String))
                }
            }
        return http.build()
    }
}
