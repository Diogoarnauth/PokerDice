package pt.isel.daw.pokerDice.repository.jdbi

import kotlinx.datetime.Instant
import org.jdbi.v3.core.Handle
import pt.isel.daw.pokerDice.domain.players.PasswordValidationInfo
import pt.isel.daw.pokerDice.domain.players.Player
import pt.isel.daw.pokerDice.domain.players.Token
import pt.isel.daw.pokerDice.domain.players.TokenValidationInfo
import pt.isel.daw.pokerDice.repository.PlayersRepository


class JdbiPlayersRepository(
    private val handle: Handle,
) : PlayersRepository {
    override fun storePlayer(username: String, passwordValidation: PasswordValidationInfo): Int {
        TODO("Not yet implemented")
    }

    override fun getPlayerByUsername(username: String): Player? {
        TODO("Not yet implemented")
    }

    override fun getPlayerById(id: Int): Player? {
        TODO("Not yet implemented")
    }

    override fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<Player, Token>? {
        TODO("Not yet implemented")
    }

    override fun isPlayerStoredByUsername(username: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun createToken(token: Token, maxTokens: Int) {
        TODO("Not yet implemented")
    }

    override fun updateTokenLastUsed(token: Token, now: Instant) {
        TODO("Not yet implemented")
    }

    override fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int {
        TODO("Not yet implemented")
    }

}