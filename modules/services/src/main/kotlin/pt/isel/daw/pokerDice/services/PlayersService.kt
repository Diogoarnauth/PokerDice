package pt.isel.daw.pokerDice.services

import jakarta.inject.Named
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import pt.isel.daw.pokerDice.domain.players.*
import pt.isel.daw.pokerDice.repository.*
import pt.isel.daw.pokerDice.utils.*

data class TokenExternalInfo(
    val tokenValue: String,
    val tokenExpiration: Instant,
)

sealed class PlayerCreationError {
    data object PlayerAlreadyExists : PlayerCreationError()

    data object InsecurePassword : PlayerCreationError()
}

typealias PlayerCreationResult = Either<PlayerCreationError, Int>

sealed class TokenCreationError {
    data object UserOrPasswordAreInvalid : TokenCreationError()
}
typealias TokenCreationResult = Either<TokenCreationError, TokenExternalInfo>


sealed class PlayerGetByIdError {
    data object PlayerNotFound : PlayerGetByIdError()
    //data class InvalidToken(val tokenValue: String) : PlayerGetByIdError() //dúvida :não sei se é necessário
}

typealias PlayerGetByIdResult = Either<PlayerGetByIdError, Player>



@Named // dúvida :falar com o stor acerca de usar service ou named
class PlayersService(
    private val transactionManager: TransactionManager, // erro
    private val playerDomain: PlayersDomain,
    private val clock: Clock // erro
) {

    fun createPlayer(
        username: String,
        name: String,
        age: Int,
        password: String,
    ): PlayerCreationResult {
        if (!playerDomain.isSafePassword(password)) {
            return failure(PlayerCreationError.InsecurePassword)
        }

        val passwordValidationInfo = playerDomain.createPasswordValidationInformation(password)

        return transactionManager.run {
            val playersRepository = it.playersRepository
            if (playersRepository.isPlayerStoredByUsername(username)) {
                failure(PlayerCreationError.PlayerAlreadyExists)
            } else {
                val id = playersRepository.storePlayer(username, passwordValidationInfo)
                success(id)
            }
        }
    }

    fun createToken(
        username: String,
        password: String,
    ): TokenCreationResult {
        if (username.isBlank() || password.isBlank()) {
            failure(TokenCreationError.UserOrPasswordAreInvalid)
        }
        return transactionManager.run {
            val playersRepository = it.playersRepository
            val player: Player =
                playersRepository.getPlayerByUsername(username)
                    ?: return@run failure(TokenCreationError.UserOrPasswordAreInvalid)
            if (!playerDomain.validatePassword(password, player.passwordValidation)) {
                if (!playerDomain.validatePassword(password, player.passwordValidation)) {
                    return@run failure(TokenCreationError.UserOrPasswordAreInvalid)
                }
            }
            val tokenValue = playerDomain.generateTokenValue()
            val now = clock.now()
            val newToken =
                Token(
                    playerDomain.createTokenValidationInformation(tokenValue),
                    player.id,
                    createdAt = now,
                    lastUsedAt = now,
                )
            playersRepository.createToken(newToken, playerDomain.maxNumberOfTokensPerUser)
            Either.Right(
                TokenExternalInfo(
                    tokenValue,
                    playerDomain.getTokenExpiration(newToken),
                ),
            )
        }
    }

    fun getById(
        @PathVariable id: Int,
    ): PlayerGetByIdResult {
        return transactionManager.run {
            val playersRepository = it.playersRepository

            val player = playersRepository.getPlayerById(id)
                ?: return@run failure(PlayerGetByIdError.PlayerNotFound)

            success(player)
        }
    }




}
