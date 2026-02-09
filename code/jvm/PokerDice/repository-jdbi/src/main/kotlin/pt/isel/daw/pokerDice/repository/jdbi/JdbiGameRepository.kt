package pt.isel.daw.pokerDice.repository.jdbi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.daw.pokerDice.domain.games.Game
import pt.isel.daw.pokerDice.repository.GamesRepository

class JdbiGamesRepository(
    private val handle: Handle,
) : GamesRepository {
    override fun createGame(game: Game): Int? {
        val sql =
            """
            INSERT INTO dbo.game (
                lobby_id,
                state,
                rounds_counter,
                nrUsers
            ) VALUES (
                :lobbyId,
                :state,
                :roundsCounter,
                :nrUsers
            )
            RETURNING id
            """.trimIndent()

        return handle
            .createUpdate(sql)
            .bind("lobbyId", game.lobbyId)
            .bind("state", game.state.name)
            .bind("roundsCounter", game.roundCounter)
            .bind("nrUsers", game.nrUsers)
            .executeAndReturnGeneratedKeys("id")
            .mapTo<Int>()
            .singleOrNull()
    }

    // ------------------ READ ------------------

    override fun addGameWinners(
        gameId: Int,
        winnerIds: List<Int>,
    ) {
        val sql =
            """
            INSERT INTO dbo.game_winner (game_id, user_id)
            VALUES (:gameId, :userId)
            ON CONFLICT DO NOTHING
            """.trimIndent()

        val update = handle.createUpdate(sql)

        winnerIds.forEach { winnerId ->
            update
                .bind("gameId", gameId)
                .bind("userId", winnerId)
                .execute()
        }
    }

    override fun getGameById(id: Int): Game? =
        handle
            .createQuery(
                """
                SELECT id, lobby_id, state, rounds_counter, nrUsers
                FROM dbo.game
                WHERE id = :id
                """.trimIndent(),
            ).bind("id", id)
            .map { rs, _ ->
                GameDbModel(
                    id = rs.getInt("id"),
                    lobbyId = rs.getInt("lobby_id"),
                    state = rs.getString("state"),
                    roundsCounter = rs.getInt("rounds_counter"),
                    nrUsers = rs.getInt("nrUsers"),
                ).toDomain()
            }.singleOrNull()

    override fun getGameByLobbyId(lobbyId: Int): Game? =
        handle
            .createQuery(
                """
                SELECT id, lobby_id, state, rounds_counter, nrUsers
                FROM dbo.game
                WHERE lobby_id = :lobbyId
                  AND state = 'RUNNING'
                """.trimIndent(),
            ).bind("lobbyId", lobbyId)
            .mapTo<GameDbModel>()
            .singleOrNull()
            ?.toDomain()

    // ------------------ UPDATE ------------------

    override fun updateGameState(
        gameId: Int,
        state: Game.GameStatus,
    ) {
        println("state : $state")
        println("state_name : ${state.name}")
        handle
            .createUpdate(
                """
                UPDATE dbo.game
                SET state = :newState
                WHERE id = :id
                """.trimIndent(),
            ).bind("id", gameId)
            .bind("newState", state.name)
            .execute()
    }

    override fun getGameWinners(gameId: Int): List<String> =
        handle
            .createQuery(
                """
                SELECT u.username
                FROM dbo.game_winner gw
                INNER JOIN dbo.users u ON gw.user_id = u.id
                WHERE gw.game_id = :gameId
                """.trimIndent(),
            )
            .bind("gameId", gameId)
            .map { rs, _ -> rs.getString("username") }
            .toList()

    override fun decrementPlayerCount(gameId: Int) {
        val sql =
            """
            UPDATE dbo.game
            SET 
                nrUsers = nrUsers - 1
            WHERE id = :gameId
            """.trimIndent()

        handle
            .createUpdate(sql)
            .bind("gameId", gameId)
            .execute()
    }

    override fun updateRoundCounter(gameId: Int) {
        println("QUERO AUMENTAR O COUNTER DE RONDAS")
        val sql =
            """
            UPDATE dbo.game
            SET 
                rounds_counter = rounds_counter + 1
            WHERE id = :gameId
            """.trimIndent()

        handle
            .createUpdate(sql)
            .bind("gameId", gameId)
            .execute()
    }

    companion object {
        private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
    }
}

// ------------------ DB Model ------------------

data class GameDbModel(
    val id: Int,
    val lobbyId: Int,
    val state: String,
    val roundsCounter: Int,
    val nrUsers: Int,
) {
    fun toDomain(): Game =
        Game(
            id = id,
            lobbyId = lobbyId,
            state = Game.GameStatus.valueOf(state),
            roundCounter = roundsCounter,
            nrUsers = nrUsers,
        )
}
