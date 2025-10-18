package pt.isel.daw.pokerDice.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
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
            .bind("winner", round.roundWinners) //
            .bind("bet", round.bet)
            .bind("roundOver", round.roundOver)
            .bind("roundNumber", round.roundNumber)
            .executeAndReturnGeneratedKeys("id")
            .mapTo<Int>()
            .one()
    }

    override fun getRoundsByGameId(gameId: Int): List<Round> {
        val sql =
            """
            SELECT id, game_id, winner, bet, roundOver, round_number
            FROM dbo.round
            WHERE game_id = :gameId
            ORDER BY round_number ASC
            """.trimIndent()

        return handle
            .createQuery(sql)
            .bind("gameId", gameId)
            .map { rs, _ ->
                Round(
                    id = rs.getInt("id"),
                    gameId = rs.getInt("game_id"),
                    roundWinners = rs.getInt("winner").takeIf { !rs.wasNull() },
                    bet = rs.getInt("bet"),
                    roundOver = rs.getBoolean("roundOver"),
                    roundNumber = rs.getInt("round_number"),
                )
            }.list()
    }
}
