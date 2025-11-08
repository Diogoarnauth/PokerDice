package pt.isel.daw.pokerDice.repository.jdbi

import kotlinx.datetime.Instant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.daw.pokerDice.domain.users.PasswordValidationInfo
import pt.isel.daw.pokerDice.domain.users.Token
import pt.isel.daw.pokerDice.domain.users.TokenValidationInfo
import pt.isel.daw.pokerDice.domain.users.User
import pt.isel.daw.pokerDice.repository.UsersRepository

class JdbiUsersRepository(
    private val handle: Handle,
) : UsersRepository {
    /* override fun storeUser(username: String, passwordValidation: PasswordValidationInfo): Int {
        TODO("Not yet implemented")
    }*/

    override fun getUserByUsername(username: String): User? =
        handle
            .createQuery("SELECT * FROM dbo.users WHERE username = :username")
            .bind("username", username)
            .mapTo<User>()
            .singleOrNull()

    override fun getUserById(id: Int): User? =
        handle
            .createQuery("SELECT * FROM dbo.Users WHERE id = :id")
            .bind("id", id)
            .mapTo<User>()
            .singleOrNull()

    override fun create(
        // testar e saber se apenas temos de retornar o id... quanto ao token??
        username: String,
        name: String,
        age: Int,
        inviteCode: String,
        passwordValidationInfo: PasswordValidationInfo,
    ): Int =
        handle
            .createUpdate(
                """
            INSERT INTO dbo.Users (username, name, age, passwordvalidation)
            VALUES (:username, :name, :age, :password)
            """,
            ).bind("username", username)
            .bind("name", name)
            .bind("age", age)
            .bind("password", passwordValidationInfo.validationInfo)
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Int::class.java)
            .one()

    override fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<User, Token>? =
        handle
            .createQuery(
                """
            SELECT 
                u.id,
                u.username,
                u.passwordValidation,
                u.name,
                u.age,
                u.credit,
                u.winCounter,
                t.tokenValidation,
                t.createdAt,
                t.lastUsedAt,
                t.userId 
            FROM dbo.Token t
            JOIN dbo.Users u ON t.userId = u.id
            WHERE t.tokenValidation = :tokenValidation
            """,
            ).bind("tokenValidation", tokenValidationInfo.validationInfo)
            .map { rs, _ ->
                UserAndTokenModel(
                    id = rs.getInt("id"),
                    username = rs.getString("username"),
                    passwordValidation = PasswordValidationInfo(rs.getString("passwordValidation")),
                    name = rs.getString("name"),
                    age = rs.getInt("age"),
                    credit = rs.getInt("credit"),
                    winCounter = rs.getInt("winCounter"),
                    tokenValidation = TokenValidationInfo(rs.getString("tokenValidation")),
                    createdAt = rs.getLong("createdAt"),
                    lastUsedAt = rs.getLong("lastUsedAt"),
                )
            }.singleOrNull()
            ?.userAndToken

    override fun isUserStoredByUsername(username: String): Boolean =
        handle
            .createQuery("SELECT COUNT(*) FROM dbo.Users WHERE username = :username")
            .bind("username", username)
            .mapTo<Int>()
            .single() == 1

    override fun countUsersInLobby(lobbyId: Int): Int =
        handle
            .createQuery("SELECT COUNT(*) FROM dbo.Users WHERE lobby_id = :lobbyId")
            .bind("lobbyId", lobbyId)
            .mapTo<Int>()
            .one()

    override fun getAllUsersInLobby(lobbyId: Int): List<User> =
        handle
            .createQuery("SELECT * FROM dbo.Users WHERE lobby_id = :lobbyId")
            .bind("lobbyId", lobbyId)
            .mapTo<User>()
            .list()

    override fun createToken(
        // testar, perceber se percebe quando o maxtokens foi atingido
        token: Token,
        maxTokens: Int,
    ) {
        // Apagar tokens antigos se exceder maxTokens
        handle
            .createUpdate(
                """
                DELETE FROM dbo.Token 
                WHERE userId = :userId 
                  AND tokenValidation IN (
                      SELECT tokenValidation 
                      FROM dbo.Token 
                      WHERE userId = :userId 
                      ORDER BY lastUsedAt DESC 
                      OFFSET :offset
                  )
                """.trimIndent(),
            ).bind("userId", token.userId)
            .bind("offset", maxTokens - 1)
            .execute()

        // Inserir novo token
        handle
            .createUpdate(
                """
                INSERT INTO dbo.Token(userId, tokenValidation, createdAt, lastUsedAt) 
                VALUES (:userId, :tokenValidation, :createdAt, :lastUsedAt)
                """.trimIndent(),
            ).bind("userId", token.userId)
            .bind("tokenValidation", token.tokenValidationInfo.validationInfo)
            .bind("createdAt", token.createdAt.epochSeconds)
            .bind("lastUsedAt", token.lastUsedAt.epochSeconds)
            .execute()
    }

    override fun updateUserCredit(
        userId: Int,
        credit: Int,
    ) {
        handle
            .createUpdate("UPDATE dbo.Users SET credit = :credit WHERE id = :userId")
            .bind("credit", credit)
            .bind("userId", userId)
            .execute()
    }

    override fun updateTokenLastUsed(
        token: Token,
        now: Instant,
    ) {
        handle
            .createUpdate(
                """
                UPDATE dbo.Token 
                SET lastUsedAt = :lastUsedAt 
                WHERE userId = :userId AND tokenValidation = :tokenValidation
                """.trimIndent(),
            ).bind("lastUsedAt", now.epochSeconds)
            .bind("userId", token.userId)
            .bind("tokenValidation", token.tokenValidationInfo.validationInfo)
            .execute()
    }

    override fun getUserTokens(userId: Int): List<Token> =
        handle
            .createQuery(
                "SELECT tokenvalidation, createdat, lastusedat, userid " +
                    "FROM dbo.Token WHERE userid = :userId",
            ).bind("userId", userId)
            .map { rs, _ ->
                val tokenValidationInfo = TokenValidationInfo(rs.getString("tokenvalidation"))
                val createdAt = Instant.fromEpochSeconds(rs.getLong("createdat"))
                val lastUsedAt = Instant.fromEpochSeconds(rs.getLong("lastusedat"))
                val userId = rs.getInt("userid")

                Token(tokenValidationInfo, userId, createdAt, lastUsedAt)
            }.list()

    override fun clearLobbyForAllUsers(lobbyId: Int) {
        handle
            .createUpdate("UPDATE dbo.Users SET lobby_id = NULL WHERE lobby_id = :lobbyId")
            .bind("lobbyId", lobbyId)
            .execute()
    }

    override fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int =
        handle
            .createUpdate(
                "DELETE FROM dbo.token WHERE tokenValidation = :tokenValidationInfo",
            ).bind("tokenValidationInfo", tokenValidationInfo.validationInfo) // Usando o valor do tokenValidationInfo
            .execute()

    override fun countUsers(): Int =
        handle
            .createQuery("SELECT COUNT(*) FROM dbo.Users")
            .mapTo<Int>()
            .one()

    private data class UserAndTokenModel(
        val id: Int,
        val username: String,
        val passwordValidation: PasswordValidationInfo,
        val name: String,
        val age: Int,
        val tokenValidation: TokenValidationInfo,
        var credit: Int,
        val winCounter: Int,
        val createdAt: Long,
        val lastUsedAt: Long,
    ) {
        val userAndToken: Pair<User, Token>
            get() =
                Pair(
                    User(id, username, passwordValidation, name, age, credit, winCounter),
                    Token(
                        tokenValidation,
                        id,
                        Instant.fromEpochSeconds(createdAt),
                        Instant.fromEpochSeconds(lastUsedAt),
                    ),
                )
    }

    override fun removeUserFromLobby(
        userId: Int,
        lobbyId: Int,
    ) {
        handle
            .createUpdate(
                """
        UPDATE dbo.users
        SET lobby_id = NULL
        WHERE id = :userId AND lobby_id = :lobbyId
        """,
            ).bind("userId", userId)
            .bind("lobbyId", lobbyId)
            .execute()
    }

    override fun decrementCreditsFromPlayer(
        amount: Int,
        userId: Int,
    ): Boolean {
        require(amount >= 0) { "amount must be >= 0" }

        // Só decrementa se o utilizador tiver saldo suficiente (credit >= amount).
        // Se não tiver, 0 linhas são afetadas → devolve false e nada muda na BD.
        val rows =
            handle
                .createUpdate(
                    """
                    UPDATE dbo.users
                    SET credit = credit - :amount
                    WHERE id = :userId
                      AND credit >= :amount
                    """.trimIndent(),
                ).bind("amount", amount)
                .bind("userId", userId)
                .execute()

        return rows == 1
    }

    override fun userExitsLobby(
        lobbyId: Int,
        userId: Int,
    ): Boolean {
        val rows =
            handle
                .createUpdate(
                    """
            UPDATE dbo.users
            SET lobby_id = NULL
            WHERE lobby_id = :lobbyId AND id = :userId
            """,
                ).bind("lobbyId", lobbyId)
                .bind("userId", userId)
                .execute()
        // Returns true if at least one row was updated
        return rows > 0
    }

    override fun updateLobbyIdForUser(
        userId: Int,
        lobbyId: Int?,
    ) {
        handle
            .createUpdate("UPDATE dbo.Users SET lobby_id = :lobbyId WHERE id = :playerId")
            .bind("lobbyId", lobbyId)
            .bind("playerId", userId)
            .execute()
    }
}
