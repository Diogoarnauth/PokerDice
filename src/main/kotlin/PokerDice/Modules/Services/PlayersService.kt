import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.example.Domain.Players.Player
import org.example.HTTP.pipeline.PlayerUris
import org.example.PokerDice.Modules.HTTP.model.PlayerTokenCreateOutputModel
import org.example.repository.TransactionManager
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import pt.isel.daw.tictactoe.domain.users.PlayersDomain


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



@Service // dúvida :falar com o stor acerca de usar service ou named
class PlayersService(
    private val transactionManager: TransactionManager,
    private val playerDomain: PlayersDomain,
    private val clock: Clock
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
            val usersRepository = it.usersRepository
            if (usersRepository.isUserStoredByUsername(username)) {
                failure(PlayerCreationError.PlayerAlreadyExists)
            } else {
                val id = usersRepository.storeUser(username, passwordValidationInfo)
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
            val usersRepository = it.usersRepository
            val player: Player =
                usersRepository.getUserByUsername(username)
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
            usersRepository.createToken(newToken, playerDomain.maxNumberOfTokensPerUser)
            Either.Right(
                TokenExternalInfo(
                    tokenValue,
                    playerDomain.getTokenExpiration(newToken),
                ),
            )
        }
    }

    @GetMapping(PlayerUris.Players.GET_BY_ID)
    fun getById(
        @PathVariable id: String,
    ) {
        TODO("TODO")
    }



}
