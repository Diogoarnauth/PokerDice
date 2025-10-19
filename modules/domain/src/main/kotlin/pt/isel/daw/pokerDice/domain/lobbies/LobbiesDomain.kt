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
        TODO()
    }

        /*
            fun markLobbyAsAvailable(
                lobby: Lobby,
                game: Game,
            ): Lobby {
                // Só lobbies que estavam fechados podem ser reabertos
                if (lobby.isRunning != false && game.state.isWaitingForPlayers != true) {
                    throw IllegalStateException(
                        "O lobby ${lobby.id} não pode ser marcado como disponível a partir do estado ${game.state.isWaitingForPlayers}",
                    )
                }

                // Regras de negócio para disponibilidade (exemplo: deve ter jogadores suficientes)
                if (lobby.minUsers < config.minUsersAllowed) {
                    throw IllegalStateException("O lobby ${lobby.id} não tem jogadores suficientes (${lobby.minUsers}/${config.minUsersAllowed})")
                }

                // Retorna uma cópia com o novo estado
                return lobby.copy(isRunning = false)
            }

         */

    fun createPasswordValidationInformation(password: String) =
        PasswordValidationInfo(
            validationInfo = passwordEncoder.encode(password),
        )

    fun isSafePassword(password: String) = password.length > 4
}
