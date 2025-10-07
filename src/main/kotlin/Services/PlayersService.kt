import org.example.Domain.Players.Player
import org.springframework.stereotype.Service
import pt.isel.daw.tictactoe.domain.users.PlayersDomain
import java.time.Clock
import java.time.Instant

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
                failure(UserCreationError.UserAlreadyExists)
            } else {
                val id = usersRepository.storeUser(username, passwordValidationInfo)
                success(id)
            }
        }
    }



}
