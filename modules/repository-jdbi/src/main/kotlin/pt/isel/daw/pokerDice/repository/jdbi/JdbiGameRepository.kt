package pt.isel.daw.pokerDice.repository.jdbi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import org.postgresql.util.PGobject
import pt.isel.daw.pokerDice.domain.games.Game
import pt.isel.daw.pokerDice.domain.games.Round
import pt.isel.daw.pokerDice.repository.GamesRepository
import java.util.UUID
import kotlin.collections.emptyList

class JdbiGamesRepository(
    private val handle: Handle,
) : GamesRepository {
    override fun createGame(game: Game): Int =
        handle
            .createUpdate(
                """
                INSERT INTO dbo.Game (
                    lobby_id,
                    state,
                    rounds_counter,
                    winner,
                    round_results
                ) VALUES (
                    :lobbyId, :state, :roundsCounter, :winner, :roundResults
                )
                RETURNING id
                """.trimIndent(),
            ).bind("lobbyId", game.lobby_id)
            .bind("state", game.state)
            .bind("roundsCounter", game.rounds_counter)
            .bind("winner", game.winner)
            .bindJson("roundResults", game.round_result)
            .mapTo<Int>()
            .one()

    override fun getGameById(id: Int): Game? =
        handle
            .createQuery(
                """
                SELECT id, lobbyId, state, roundsCounter, winner, roundResults
                FROM dbo.Game
                WHERE id = :id
                """.trimIndent(),
            ).bind("id", id)
            .mapTo<GameDbModel>()
            .singleOrNull()
            ?.toDomain()

    override fun getGameByLobbyId(lobbyId: Int): Game? =
        handle
            .createQuery(
                """
                SELECT id, lobbyId, state, roundsCounter, winner, roundResults
                FROM dbo.Game
                WHERE lobbyId = :lobbyId
                  AND state = 'RUNNING'
                """.trimIndent(),
            ).bind("lobbyId", lobbyId)
            .mapTo<GameDbModel>()
            .singleOrNull()
            ?.toDomain()

    override fun updateGameState(
        gameId: Int,
        newState: String,
    ) {
        handle
            .createUpdate(
                """
                UPDATE dbo.Game
                SET state = :newState
                WHERE id = :id
                """.trimIndent(),
            ).bind("id", gameId)
            .bind("newState", newState)
            .execute()
    }

    // ---------- Helpers ----------
    private fun org.jdbi.v3.core.statement.Update.bindJson(
        name: String,
        value: Any?,
    ): org.jdbi.v3.core.statement.Update {
        val json = value?.let { objectMapper.writeValueAsString(it) }
        return bind(
            name,
            PGobject().apply {
                type = "jsonb"
                this.value = json
            },
        )
    }

    companion object {
        private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
    }
}

// ---------- Data model intermédio (DB <-> domínio) ----------
data class GameDbModel(
    val id: UUID,
    val lobbyId: UUID,
    val state: Game.State,
    val roundsCounter: Int,
    val winner: Int?,
    val roundResults: Int?,
) {
    fun toDomain(): Game {
        val mapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
        val roundResultsObj = roundResults?.let { mapper.readValue<List<String>>(it) } ?: emptyList()

        return Game(
            id = id,
            lobby_id = lobbyId,
            state = state,
            rounds_counter = roundsCounter,
            winner = winner,
            round_result = Int,
        )
    }
}
