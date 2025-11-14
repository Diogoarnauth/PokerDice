package pt.isel.daw.pokerDice.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.daw.pokerDice.domain.games.Round
import pt.isel.daw.pokerDice.repository.RoundRepository

class JdbiRoundRepository(
    private val handle: Handle,
) : RoundRepository {
    /*
    override fun updateRound(
            roundId: Int,
            winner: Int,
            bet:Int,
            roundOver: Boolean,
        ) {
            handle
                .createUpdate(
                    """
        UPDATE dbo.round
        SET winner = :winner,
            bet = :bet,
            roundOver = :roundOver
        WHERE id = :roundId
        """,
                ).bind("rollCount", rollCount)
                .bind("diceFaces", diceResults) // Pode ser uma lista; se for texto, converte para CSV ou JSON
                .bind("isDone", isDone)
                .bind("turnId", turnId)
                .execute()
        }
    }

game_id INT REFERENCES dbo.game(id) ON DELETE CASCADE,
winner INT REFERENCES dbo.users(id) ON DELETE SET NULL,
bet INT NOT NULL CHECK (bet >= 10),
roundOver BOOLEAN DEFAULT FALSE,
round_number INT NOT NULL
*/

    override fun createRound(
        gameId: Int,
        round: Round,
    ): Int {
        val sql =
            """
            INSERT INTO dbo.round (
                game_id,
                bet,
                roundOver,
                round_number
            ) VALUES (
                :gameId,
                :bet,
                :roundOver,
                :roundNumber
            )
            RETURNING id
            """.trimIndent()

        return handle
            .createUpdate(sql)
            .bind("gameId", gameId)
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
            SELECT id, game_id, bet, roundOver, round_number
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
                    bet = rs.getInt("bet"),
                    roundOver = rs.getBoolean("roundOver"),
                    roundNumber = rs.getInt("round_number"),
                )
            }.list()
    }

    override fun markRoundAsOver(roundId: Int) {
        val sql =
            """
            UPDATE dbo.round
            SET roundOver = TRUE
            WHERE id = :roundId
            """.trimIndent()

        handle
            .createUpdate(sql)
            .bind("roundId", roundId)
            .execute()
    }

    override fun attributeWinnerStatus(
        roundId: Int,
        winnerIds: List<Int>,
    ) {
        val sql =
            """
            INSERT INTO dbo.round_winner (round_id, user_id)
            VALUES (:roundId, :userId)
            ON CONFLICT DO NOTHING
            """.trimIndent()

        val update = handle.createUpdate(sql)

        winnerIds.forEach { winnerId ->
            update
                .bind("roundId", roundId)
                .bind("userId", winnerId)
                .execute()
        }
    }

    override fun getGameWinner(gameId: Int): List<Int> {
        val sql =
            """
            SELECT rw.user_id, COUNT(*) AS win_count
            FROM dbo.round_winner rw
            JOIN dbo.round r ON rw.round_id = r.id
            WHERE r.game_id = :gameId
            GROUP BY rw.user_id
            ORDER BY win_count DESC
            """.trimIndent()

        val results =
            handle
                .createQuery(sql)
                .bind("gameId", gameId)
                .mapToMap()
                .list()

        if (results.isEmpty()) {
            return emptyList()
        }

        val maxWins = results.maxOf { (it["win_count"] as Number).toInt() }

        val winners =
            results
                .filter { (it["win_count"] as Number).toInt() == maxWins }
                .map { it["user_id"] as Int }

        return winners
    }
}
