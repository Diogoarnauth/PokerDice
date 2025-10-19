package pt.isel.daw.pokerDice.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.daw.pokerDice.domain.games.Turn
import pt.isel.daw.pokerDice.repository.TurnsRepository

class JdbiTurnRepository(
    private val handle: Handle,
) : TurnsRepository {
    override fun createTurn(
        roundId: Int,
        turn: Turn,
    ): Int =
        handle
            .createUpdate(
                """
            INSERT INTO dbo.turn (round_id, player_id, roll_count, dice_faces, is_done)
            VALUES (:roundId, :playerId, :rollCount, :diceFaces, :isDone)
            """,
            ).bind("roundId", roundId)
            .bind("playerId", turn.playerId)
            .bind("rollCount", turn.rollCount)
            .bind("diceFaces", turn.diceFaces)
            .bind("isDone", turn.isDone)
            .executeAndReturnGeneratedKeys("id")
            .mapTo<Int>()
            .first()
}
