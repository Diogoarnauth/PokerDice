package pt.isel.daw.pokerDice.domain.users

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.Base64
import kotlin.time.toJavaDuration

@Component
class UsersDomain(
    private val passwordEncoder: PasswordEncoder,
    private val tokenEncoder: TokenEncoder,
    private val config: UsersDomainConfig,
) {
    fun generateTokenValue(): String =
        ByteArray(config.tokenSizeInBytes).let { byteArray ->
            SecureRandom.getInstanceStrong().nextBytes(byteArray)
            Base64.getUrlEncoder().encodeToString(byteArray)
        }

    fun canBeToken(token: String): Boolean =
        try {
            Base64
                .getUrlDecoder()
                .decode(token)
                .size == config.tokenSizeInBytes
        } catch (ex: IllegalArgumentException) {
            false
        }

    fun validatePassword(
        password: String,
        validationInfo: PasswordValidationInfo,
    ) = passwordEncoder.matches(
        password,
        validationInfo.validationInfo,
    )

    fun createPasswordValidationInformation(password: String) =
        PasswordValidationInfo(
            validationInfo = passwordEncoder.encode(password),
        )

    fun isTokenTimeValid(
        clock: Clock,
        token: Token,
    ): Boolean {
        val now = Instant.now(clock)
        println("isTokenValid")
        println("createdAt = ${token.createdAt}")
        println("lastUsedAt = ${token.lastUsedAt}")
        println("tokenTtl = ${config.tokenTtl}")
        println("tokenRollingTtl = ${config.tokenRollingTtl}")

        val response =
            token.createdAt <= now &&
                Duration.between(token.createdAt, now) <= config.tokenTtl.toJavaDuration() &&
                Duration.between(token.lastUsedAt, now) <= config.tokenRollingTtl.toJavaDuration()

        println("response: $response")
        return response
    }

    fun getTokenExpiration(token: Token): Instant {
        val absoluteExpiration = token.createdAt.plus(config.tokenTtl.toJavaDuration())
        val rollingExpiration = token.lastUsedAt.plus(config.tokenRollingTtl.toJavaDuration())
        return if (absoluteExpiration < rollingExpiration) {
            absoluteExpiration
        } else {
            rollingExpiration
        }
    }

    fun createTokenValidationInformation(token: String): TokenValidationInfo = tokenEncoder.createValidationInformation(token)

    fun isSafePassword(password: String) = password.length > 4

    val maxNumberOfTokensPerUser = config.maxTokensPerUser
}
