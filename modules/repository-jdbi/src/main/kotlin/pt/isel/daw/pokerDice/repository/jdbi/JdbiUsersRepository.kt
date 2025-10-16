package pt.isel.daw.pokerDice.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.daw.pokerDice.domain.users.PasswordValidationInfo
import pt.isel.daw.pokerDice.domain.users.Token
import pt.isel.daw.pokerDice.domain.users.TokenValidationInfo
import pt.isel.daw.pokerDice.domain.users.User
import pt.isel.daw.pokerDice.repository.UsersRepository
import java.sql.Timestamp
import java.time.Instant

class JdbiUsersRepository(
    private val handle: Handle,
) : UsersRepository {
    override fun getUserByUsername(username: String): User? =
        handle
            .createQuery("SELECT * FROM dbo.Users WHERE username = :username")
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

    override fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<User, Token>? {
        val response =
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
                    val user =
                        User(
                            id = rs.getInt("id"),
                            username = rs.getString("username"),
                            passwordValidation = PasswordValidationInfo(rs.getString("passwordValidation")),
                            name = rs.getString("name"),
                            age = rs.getInt("age"),
                            credit = rs.getInt("credit"),
                            winCounter = rs.getInt("winCounter"),
                        )

                    val token =
                        Token(
                            tokenValidationInfo = TokenValidationInfo(rs.getString("tokenValidation")),
                            createdAt = rs.getTimestamp("createdAt").toInstant(),
                            lastUsedAt = rs.getTimestamp("lastUsedAt").toInstant(),
                            userId = rs.getInt("userId"),
                        )

                    Pair(user, token)
                }.findOne()
                .orElse(null)

        return response
    }

    override fun isUserStoredByUsername(username: String): Boolean =
        handle
            .createQuery("SELECT COUNT(*) FROM dbo.Users WHERE username = :username")
            .bind("username", username)
            .mapTo<Int>()
            .single() == 1

    //
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

    override fun countUsersInLobby(lobbyId: Int): Int =
        handle
            .createQuery("SELECT COUNT(*) FROM dbo.Users WHERE lobby_id = :lobbyId")
            .bind("lobbyId", lobbyId)
            .mapTo<Int>()
            .one()

    override fun createToken(
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
            .bind("createdAt", Timestamp.from(token.createdAt))
            .bind("lastUsedAt", Timestamp.from(token.lastUsedAt))
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
            ).bind("lastUsedAt", Timestamp.from(now))
            .bind("userId", token.userId)
            .bind("tokenValidation", token.tokenValidationInfo.validationInfo)
            .execute()
    }

    override fun clearLobbyForAllUsers(lobbyId: Int) {
        handle
            .createUpdate("UPDATE dbo.Users SET lobby_id = NULL WHERE lobby_id = :lobbyId")
            .bind("lobbyId", lobbyId)
            .execute()
    }

    override fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int {
        TODO("Not yet implemented")
    }

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
                        Instant.ofEpochSecond(createdAt),
                        Instant.ofEpochSecond(lastUsedAt),
                    ),
                )
    }
}
