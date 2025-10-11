package pt.isel.daw.pokerDice.services

import jakarta.inject.Named
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.springframework.web.bind.annotation.PathVariable
import pt.isel.daw.pokerDice.domain.Invite.InviteDomain
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
    data object PlayerOrPasswordAreInvalid : TokenCreationError()
}
typealias TokenCreationResult = Either<TokenCreationError, TokenExternalInfo>


sealed class PlayerGetByIdError {
    data object PlayerNotFound : PlayerGetByIdError()
    //data class InvalidToken(val tokenValue: String) : PlayerGetByIdError() //dúvida :não sei se é necessário
}

typealias PlayerGetByIdResult = Either<PlayerGetByIdError, Player>

typealias PlayerRegisterResult = Either<PlayerRegisterError, Int>

sealed class PlayerRegisterError {
    data object InvitationDontExist : PlayerRegisterError()

    data object InvitationExpired : PlayerRegisterError()

    data object InvitationUsed : PlayerRegisterError()

    data object PlayerAlreadyExists : PlayerRegisterError()

    data object InsecurePassword : PlayerRegisterError()
}

sealed class CreatingAppInviteError {
    data object CreatingInviteError : CreatingAppInviteError()
}

typealias CreatingAppInviteResult = Either<CreatingAppInviteError, String>



@Named // dúvida :falar com o stor acerca de usar service ou named
class PlayersService(
    private val transactionManager: TransactionManager, // erro
    private val playerDomain: PlayersDomain,
    private val inviteDomain: InviteDomain,
    private val clock: Clock // erro
) {

    fun createAppInvite(playerId: Int): CreatingAppInviteResult =
        transactionManager.run {
            val inviteRepository = it.inviteRepository
            val newInvite = inviteDomain.generateInviteValue()
            val inviteValidationInfo = inviteDomain.createInviteValidationInformation(newInvite)
            val state = inviteDomain.validState
            val now = clock.now()
            val invite = inviteRepository.createAppInvite(playerId, inviteValidationInfo, state, now)
            if (invite == null) {
                failure(CreatingAppInviteError.CreatingInviteError)
            } else {
                success(newInvite)
            }
        }


    fun createPlayer(
        username: String,
        name: String,
        age: Int,
        password: String,
        inviteCode:String
    ): PlayerRegisterResult {

        if (!playerDomain.isSafePassword(password)) {
            return failure(PlayerRegisterError.InsecurePassword)
        }

        val inviteCodeValidationInfo = inviteDomain.createInviteValidationInformation(inviteCode)
        val passwordValidationInfo = playerDomain.createPasswordValidationInformation(password)

        return transactionManager.run {

            val playersRepository = it.playersRepository
            val inviteRepository = it.inviteRepository
            val invite = inviteRepository.getAppInviteByValidationInfo(inviteCodeValidationInfo)

            if (playersRepository.isPlayerStoredByUsername(username)) {
                return@run failure(PlayerRegisterError.PlayerAlreadyExists)
            }

            if (invite == null) {
                failure(PlayerRegisterError.InvitationDontExist)
            } else if (!inviteDomain.isInviteCodeValid(invite.state)) {
                failure(PlayerRegisterError.InvitationUsed)
            } else if (!inviteDomain.isInviteTimeNotExpired(invite.createdAt, clock)) {
                inviteRepository.changeInviteState( invite.id, inviteDomain.expiredState)
                failure(PlayerRegisterError.InvitationExpired)
            } else {
                val playerId = playersRepository.create(username,name,age,inviteCode, passwordValidationInfo)
                inviteRepository.changeInviteState( invite.id, inviteDomain.usedState)
                success(playerId)
            }
        }

    }


    fun createToken(
        username: String,
        password: String,
    ): TokenCreationResult {
        if (username.isBlank() || password.isBlank()) {
            failure(TokenCreationError.PlayerOrPasswordAreInvalid)
        }
        return transactionManager.run {
            val playersRepository = it.playersRepository
            val player: Player =
                playersRepository.getPlayerByUsername(username)
                    ?: return@run failure(TokenCreationError.PlayerOrPasswordAreInvalid)
            if (!playerDomain.validatePassword(password, player.passwordValidation)) {
                if (!playerDomain.validatePassword(password, player.passwordValidation)) {
                    return@run failure(TokenCreationError.PlayerOrPasswordAreInvalid)
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
            playersRepository.createToken(newToken, playerDomain.maxNumberOfTokensPerPlayer)
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

    fun getPlayerByToken(token: String): Player? {
        if (!playerDomain.canBeToken(token)) {
            return null
        }
        return transactionManager.run {
            val usersRepository = it.playersRepository
            val tokenValidationInfo = playerDomain.createTokenValidationInformation(token)
            val userAndToken = usersRepository.getTokenByTokenValidationInfo(tokenValidationInfo)
            if (userAndToken != null && playerDomain.isTokenTimeValid(clock, userAndToken.second)) {
                usersRepository.updateTokenLastUsed(userAndToken.second, clock.now())
                userAndToken.first
            } else {
                null
            }
        }
    }

    fun hasAnyPlayer(): Boolean = transactionManager.run {
        val playersRepository = it.playersRepository
        return@run playersRepository.countPlayers() > 0
    }

    fun bootstrapFirstPlayer(username : String, name : String, age : Int, password : String): Int =
        transactionManager.run {
            val playersRepository = it.playersRepository
            val passwordValidationInfo = playerDomain.createPasswordValidationInformation(password)
            playersRepository.create(
                username = username,
                name = name,
                age = age,
                inviteCode = "BOOTSTRAP",
                passwordValidationInfo = passwordValidationInfo
            )
        }








}
