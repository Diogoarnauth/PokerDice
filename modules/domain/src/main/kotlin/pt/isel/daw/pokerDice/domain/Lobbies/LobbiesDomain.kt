package pt.isel.daw.pokerDice.domain.Lobbies

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import pt.isel.daw.pokerDice.domain.players.PasswordValidationInfo

@Component
class LobbiesDomain(
    private val config: LobbiesDomainConfig,
    private val passwordEncoder: PasswordEncoder,
) {

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

    fun isSafePassword(password: String) = password.length > 4



}
