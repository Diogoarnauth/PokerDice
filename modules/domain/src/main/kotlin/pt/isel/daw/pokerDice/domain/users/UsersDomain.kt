package pt.isel.daw.pokerDice.domain.users

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.Base64

@Component
class UsersDomain(
    private val passwordEncoder: PasswordEncoder,
    private val tokenEncoder: TokenEncoder,
    private val config: UsersDomainConfig,
) {
    val maxNumberOfTokensPerUser = config.maxTokensPerUser

    fun isUsernameValid(username: String): Boolean =
        username.isNotBlank() &&
            username.length >= config.minUsernameLength

    fun isNameValid(name: String): Boolean = name.isNotBlank()

    fun isAgeValid(age: Int): Boolean = age in config.minAge..config.maxAge

    fun isSafePassword(password: String): Boolean {
        if (password.length < config.minPasswordLength) {
            return false
        }

        val hasDigit = password.any { it.isDigit() }
        val hasUpperCase = password.any { it.isUpperCase() }

        return hasDigit && hasUpperCase
    }

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
        val now = clock.now()
        return token.createdAt <= now &&
            (now - token.createdAt) <= config.tokenTtl &&
            (now - token.lastUsedAt) <= config.tokenRollingTtl
    }

    fun getTokenExpiration(token: Token): Instant {
        val absoluteExpiration = token.createdAt + config.tokenTtl
        val rollingExpiration = token.lastUsedAt + config.tokenRollingTtl
        return if (absoluteExpiration < rollingExpiration) {
            absoluteExpiration
        } else {
            rollingExpiration
        }
    }

    fun createTokenValidationInformation(token: String): TokenValidationInfo = tokenEncoder.createValidationInformation(token)
}
