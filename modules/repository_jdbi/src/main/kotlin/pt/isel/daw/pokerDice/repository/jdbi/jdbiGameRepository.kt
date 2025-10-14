package pt.isel.daw.pokerDice.repository.jdbi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import org.jdbi.v3.core.statement.Update
import org.postgresql.util.PGobject
import pt.isel.daw.pokerDice.domain.games.*
import pt.isel.daw.pokerDice.domain.users.User
import pt.isel.daw.pokerDice.repository.GamesRepository
import java.util.*

class JdbiGamesRepository(
    private val handle: Handle,
) : GamesRepository {

    override fun insert(game: Game) {
        handle.createUpdate(
            """
            insert into dbo.games(
                id, state, nr_players, min_credits,
                players, rounds, scores,
                current_player_index, last_roll, last_combination
            ) values (
                :id, :state, :nrPlayers, :minCredits,
                :players, :rounds, :scores,
                :currentPlayerIndex, :lastRoll, :lastCombination
            )
            """.trimIndent()
        )
            .bind("id", game.id)
            .bind("state", game.state.name)
            .bind("nrPlayers", game.nrPlayers)
            .bind("minCredits", game.minCredits)
            .bindJson("players", game.players)
            .bindJson("rounds", game.rounds)
            .bindJson("scores", game.scores)
            .bind("currentPlayerIndex", game.currentPlayerIndex)
            .bindJson("lastRoll", game.lastRoll)
            .bind("lastCombination", game.lastCombination?.name)
            .execute()
    }

    override fun getById(id: UUID): Game? {
        return handle.createQuery(
            """
            select * from dbo.games where id = :id
            """.trimIndent()
        )
            .bind("id", id)
            .mapTo<GameDbModel>()
            .singleOrNull()
            ?.toDomain()
    }

    override fun update(game: Game) {
        handle.createUpdate(
            """
            update dbo.games set
                state = :state,
                players = :players,
                rounds = :rounds,
                scores = :scores,
                current_player_index = :currentPlayerIndex,
                last_roll = :lastRoll,
                last_combination = :lastCombination
            where id = :id
            """.trimIndent()
        )
            .bind("id", game.id)
            .bind("state", game.state.name)
            .bindJson("players", game.players)
            .bindJson("rounds", game.rounds)
            .bindJson("scores", game.scores)
            .bind("currentPlayerIndex", game.currentPlayerIndex)
            .bindJson("lastRoll", game.lastRoll)
            .bind("lastCombination", game.lastCombination?.name)
            .execute()
    }

    // --- helpers ---
    private fun Update.bindJson(name: String, value: Any?): Update {
        val json = value?.let { objectMapper.writeValueAsString(it) }
        return bind(name, PGobject().apply {
            type = "jsonb"
            this.value = json
        })
    }

    companion object {
        private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
    }
}

/**
 * Modelo intermédio entre DB e domínio
 */
data class GameDbModel(
    val id: UUID,
    val lobbyId: UUID,
    val state: String,
    val nrPlayers: Int,
    val minCredits: Int,
    val players: String,
    val rounds: String,
    val scores: String,
    val currentPlayerIndex: Int,
    val lastRoll: String,
    val lastCombination: String?
) {
    fun toDomain(): Game {
        val mapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

        val playersList: List<User> = mapper.readValue(players)
        val roundsList: List<Round> = mapper.readValue(rounds)
        val scoresMap: MutableMap<User, Int> = mapper.readValue(scores)
        val rollList: List<Dice> = mapper.readValue(lastRoll)

        return Game(
            id = id,
            lobbyId = lobbyId,
            state = Game.State.valueOf(state),
            nrPlayers = nrPlayers,
            minCredits = minCredits,
            players = playersList,
            rounds = roundsList,
            scores = scoresMap,
            currentPlayerIndex = currentPlayerIndex,
            lastRoll = rollList,
            lastCombination = lastCombination?.let { CombinationType.valueOf(it) }
        )
    }
}