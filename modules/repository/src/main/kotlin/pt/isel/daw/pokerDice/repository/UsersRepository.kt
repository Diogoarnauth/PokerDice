package pt.isel.daw.pokerDice.repository

import kotlinx.datetime.Instant
import pt.isel.daw.pokerDice.domain.users.PasswordValidationInfo
import pt.isel.daw.pokerDice.domain.users.Token
import pt.isel.daw.pokerDice.domain.users.TokenValidationInfo
import pt.isel.daw.pokerDice.domain.users.User

// mudar depois

interface UsersRepository {
    /*fun storeUser(
        username: String,
        passwordValidation: PasswordValidationInfo,
    ): Int*/

    fun getUserByUsername(username: String): User?

    fun getUserById(id: Int): User?

    fun create(
        username: String,
        name: String,
        age: Int,
        inviteCode: String,
        passwordValidationInfo: PasswordValidationInfo,
    ): Int

    fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<User, Token>?

    fun isUserStoredByUsername(username: String): Boolean

    fun updateUserCredit(
        userId: Int,
        credit: Int,
    )

    fun createToken(
        token: Token,
        maxTokens: Int,
    )

    fun countUsers(): Int

    fun updateTokenLastUsed(
        token: Token,
        now: Instant,
    )

    fun removeUserFromLobby(
        userId: Int,
        lobbyId: Int,
    )

    fun clearLobbyForAllUsers(lobbyId: Int)

    fun getAllUsersInLobby(lobbyId: Int): List<User>

    fun countUsersInLobby(lobbyId: Int): Int

    fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int
}
