package pt.isel.daw.pokerDice.repository

import kotlinx.datetime.Instant
import pt.isel.daw.pokerDice.domain.players.*


interface PlayersRepository {
    fun storePlayer(
        username: String,
        passwordValidation: PasswordValidationInfo,
    ): Int

    fun getPlayerByUsername(username: String): Player?

    fun getPlayerById (id: Int): Player?


    fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<Player, Token>?

    fun isPlayerStoredByUsername(username: String): Boolean


    fun createToken(
        token: Token,
        maxTokens: Int,
    )

    fun updateTokenLastUsed(
        token: Token,
        now: Instant,
    )

    fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int
}
