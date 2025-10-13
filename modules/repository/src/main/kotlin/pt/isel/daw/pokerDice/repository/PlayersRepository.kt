package pt.isel.daw.pokerDice.repository

import kotlinx.datetime.Instant
import pt.isel.daw.pokerDice.domain.players.*

//mudar depois

interface PlayersRepository {
    /*fun storePlayer(
        username: String,
        passwordValidation: PasswordValidationInfo,
    ): Int*/

    fun getPlayerByUsername(username: String): Player?

    fun getPlayerById (id: Int): Player?

    fun create(username: String,name: String,age: Int,inviteCode: String, passwordValidationInfo: PasswordValidationInfo,): Int

    fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<Player, Token>?

    fun isPlayerStoredByUsername(username: String): Boolean

    fun updateLobbyIdForPlayer(playerId: Int, lobbyId: Int?)

    fun createToken(
        token: Token,
        maxTokens: Int,
    )

    fun countPlayers():Int

    fun updateTokenLastUsed(
        token: Token,
        now: Instant,
    )

    fun clearLobbyForAllPlayers(lobbyId: Int)

    fun countPlayersInLobby(lobbyId: Int): Int

    fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int
}
