package pt.isel.daw.pokerDice.repository.jdbi
import org.jdbi.v3.core.Handle
import pt.isel.daw.pokerDice.domain.games.Round
import pt.isel.daw.pokerDice.repository.RoundRepository

class JdbiRoundRepository(
    private val handle: Handle,
) : RoundRepository {
    override fun createRound(
        gameId: Int,
        round: Round,
    ): Int {
        val sql =
            """
            INSERT INTO dbo.round (
                game_id,
                winner,
                bet,
                roundOver,
                round_number
            ) VALUES (
                :gameId,
                :winner,
                :bet,
                :roundOver,
                :roundNumber
            )
            RETURNING id
            """.trimIndent()

        return handle
            .createUpdate(sql)
            .bind("gameId", gameId)
            .bind("winner", round.winner)
            .bind("bet", round.bet)
            .bind("roundOver", round.roundOver)
            .bind("roundNumber", round.roundNumber)
            .executeAndReturnGeneratedKeys("id")
            .mapTo<Int>()
            .single()
    }

    override fun getRoundsByGameId(
        gameId: Int,
        page: Int,
        pageSize: Int,
    ): List<Round> {
        val offset = (page - 1) * pageSize

        val sql = """
            SELECT id, game_id, winner, bet, roundOver, round_number
            FROM dbo.round
            WHERE game_id = :gameId
            ORDER BY round_number ASC
            LIMIT :pageSize OFFSET :offset
        """.trimIndent()

        return handle
            .createQuery(sql)
            .bind("gameId", gameId)
            .bind("pageSize", pageSize)
            .bind("offset", offset)
            .mapTo<RoundDbModel>()
            .map { it.toDomain() }
            .list()
    }
}
}
