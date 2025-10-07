package org.example.repository

import PasswordValidationInfo
import Token
import TokenValidationInfo
import kotlinx.datetime.Instant
import org.example.Domain.Players.Player


interface PlayersRepository {
    fun storeUser(
        username: String,
        passwordValidation: PasswordValidationInfo,
    ): Int

    fun getUserByUsername(username: String): Player?

    fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<Player, Token>?

    fun isUserStoredByUsername(username: String): Boolean

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
