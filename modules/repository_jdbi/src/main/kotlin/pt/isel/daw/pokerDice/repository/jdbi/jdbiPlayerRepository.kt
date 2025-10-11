package pt.isel.daw.pokerDice.repository.jdbi

import kotlinx.datetime.Instant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.daw.pokerDice.domain.players.PasswordValidationInfo
import pt.isel.daw.pokerDice.domain.players.Player
import pt.isel.daw.pokerDice.domain.players.Token
import pt.isel.daw.pokerDice.domain.players.TokenValidationInfo
import pt.isel.daw.pokerDice.repository.PlayersRepository
import java.util.*


class JdbiPlayersRepository(
    private val handle: Handle,
) : PlayersRepository {

    /* override fun storePlayer(username: String, passwordValidation: PasswordValidationInfo): Int {
        TODO("Not yet implemented")
    }
        */

    override fun getPlayerByUsername(username: String): Player? =
        handle
            .createQuery("select * from dbo.Player where username = :username")
            .bind("username", username)
            .mapTo<Player>()
            .singleOrNull()


    override fun getPlayerById(id: Int): Player? =
        handle
            .createQuery("select * from dbo.Player where id = :id")
            .bind("id", id)
            .mapTo<Player>()
            .singleOrNull()


    override fun create( // testar e saber se apenas temos de retornar o id... quanto ao token??
        username: String,
        name: String,
        age: Int,
        inviteCode: String,
        passwordValidationInfo: PasswordValidationInfo
    ): Int {
        return handle
            .createUpdate(
                """
            insert into dbo."User" (username, name, age, password)
            values (:username, :name, :age, :password)
            """
            )
            .bind("username", username)
            .bind("name", name)
            .bind("age", age)
            .bind("password", passwordValidationInfo.validationInfo)
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Int::class.java)
            .one()
    }


    override fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<Player, Token>? =
        handle
            .createQuery(
                """
                select id,token, username, name, age, password, passwordValidation,tokenValidation ,createdAt,lastUsedAt, credit, winCounter from dbo.Token
                join dbo.User on dbo.Token.userid = dbo.User.id
                where tokenvalidation = :tokenValidation
                """.trimIndent(),
            ).bind("tokenValidation", tokenValidationInfo.validationInfo)
            .mapTo<PlayerAndTokenModel>()
            .singleOrNull()
            ?.playerAndToken



    override fun isPlayerStoredByUsername(username: String): Boolean =
        handle
            .createQuery("select count(*) from dbo.User where username = :username")
            .bind("username", username)
            .mapTo<Int>()
            .single() == 1


    override fun createToken( // testar, perceber se percebe quando o maxtokens foi atingido
        token: Token,
        maxTokens: Int,
    ) {
        handle
            .createUpdate(
                """
                delete from dbo.Token 
                where userId = :userId 
                    and tokenValidation in (
                        select tokenValidation from dbo.Token where userId = :userId 
                            order by lastUsedAt desc offset :offset );
                """.trimIndent(),
            ).bind("userId", token.playerId)
            .bind("offset", maxTokens - 1)
            .execute()

        handle
            .createUpdate(
                """
                insert into dbo.Token(userId, tokenValidation, createdAt, lastUsedAt) 
                values (:userId, :tokenValidation, :createdAt, :lastUsedAt)
                """.trimIndent(),
            ).bind("userId", token.playerId)
            .bind("tokenValidation", token.tokenValidationInfo.validationInfo)
            .bind("createdAt", token.createdAt.epochSeconds)
            .bind("lastUsedAt", token.lastUsedAt.epochSeconds)
            .execute()
    }

    override fun updateTokenLastUsed(token: Token, now: Instant) {
        handle
            .createUpdate(
                """
                update dbo.Token 
                set lastUsedAt = :lastUsedAt 
                where userId = :userId and tokenValidation = :tokenValidation
                """.trimIndent(),
            ).bind("lastUsedAt", now.epochSeconds)
            .bind("userId", token.playerId)
            .bind("tokenValidation", token.tokenValidationInfo.validationInfo)
            .execute()
    }

    override fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int {
        TODO("Not yet implemented")
    }

    override fun countPlayers(): Int =
        handle.createQuery("SELECT COUNT(*) FROM dbo.Player")
            .mapTo<Int>()
            .one()



    private data class PlayerAndTokenModel(
        val id: Int,
        val token: UUID,
        val username: String,
        val name : String,
        val age: Int,
        val passwordValidation: PasswordValidationInfo, //possivelmente retirar depois
        val tokenValidation: TokenValidationInfo,
        val createdAt: Long,
        val lastUsedAt: Long,
        var credit: Int,
        var winCounter: Int
    ) {
        val playerAndToken: Pair<Player, Token>
            get() =
                Pair(
                    Player(id, token,username, passwordValidation, name,age, credit, winCounter),
                    Token(
                        tokenValidation,
                        id,
                        Instant.fromEpochSeconds(createdAt),
                        Instant.fromEpochSeconds(lastUsedAt),
                    ),
                )
    }



}