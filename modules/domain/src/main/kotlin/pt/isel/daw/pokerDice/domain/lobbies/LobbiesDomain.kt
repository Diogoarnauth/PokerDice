package pt.isel.daw.pokerDice.domain.lobbies

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import pt.isel.daw.pokerDice.domain.users.PasswordValidationInfo

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

    fun countPlayersInLobby(lobbyID: Int): List<Int> {
        TODO("Implement lobby availability management")
    }

    fun markLobbyAsAvailable(lobbyID: Int) {
        TODO("Implement lobby availability management")
    }

    fun createPasswordValidationInformation(password: String) =
        PasswordValidationInfo(
            validationInfo = passwordEncoder.encode(password),
        )

    fun isSafePassword(password: String) = password.length > 4
}
