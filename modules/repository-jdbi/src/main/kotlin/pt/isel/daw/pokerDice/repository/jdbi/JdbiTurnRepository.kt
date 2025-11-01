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

    override fun updateTurn(
        turnId: Int,
        rollCount: Int,
        diceResults: String,
        isDone: Boolean,
    ) {
        handle
            .createUpdate(
                """
        UPDATE dbo.turn
        SET roll_count = :rollCount,
            dice_faces = :diceFaces,
            is_done = :isDone
        WHERE id = :turnId
        """,
            ).bind("rollCount", rollCount)
            .bind("diceFaces", diceResults) // Pode ser uma lista; se for texto, converte para CSV ou JSON
            .bind("isDone", isDone)
            .bind("turnId", turnId)
            .execute()
    }

    override fun getTurnsByRoundId(
        roundId: Int,
        userId: Int,
    ): Turn =
        handle
            .createQuery(
                """
        SELECT id, round_id, player_id, roll_count, dice_faces, is_done
        FROM dbo.turn
        WHERE round_id = :roundId
          AND player_id = :userId
        ORDER BY id
        """,
            ).bind("roundId", roundId)
            .bind("userId", userId)
            .mapTo<Turn>()
            .first()

    override fun getNextPlayerInRound(
        roundId: Int,
        lobbyId: Int,
        currTurnPlayerId: Int,
    ): Int? {
        val playersInLobby =
            handle
                .createQuery(
                    """
            SELECT u.id
            FROM dbo.users u
            WHERE u.lobby_id = :lobbyId
            ORDER BY u.id
            """,
                ).bind("lobbyId", lobbyId)
                .mapTo<Int>()
                .list()

        if (playersInLobby.isEmpty()) return null

        val turnsInRound =
            handle
                .createQuery(
                    """
            SELECT player_id
            FROM dbo.turn
            WHERE round_id = :roundId
              AND is_done = true
            """,
                ).bind("roundId", roundId)
                .mapTo<Int>()
                .list()

        // Encontra o próximo jogador que ainda não jogou
        return playersInLobby.firstOrNull { it !in turnsInRound }
    }

    override fun getTurnsByRoundIdForAllPlayers(roundId: Int): List<Turn> =
        handle
            .createQuery(
                """
        SELECT id, round_id, player_id, roll_count, dice_faces, is_done
        FROM dbo.turn
        WHERE round_id = :roundId
        """,
            ).bind("roundId", roundId)
            .mapTo<Turn>()
            .list()

    override fun getPlayersWithTurns(roundId: Int): List<Pair<Int, String>> {
        val sql =
            """
            SELECT player_id, dice_faces
            FROM dbo.turn
            WHERE round_id = :roundId
            AND is_done = true
            ORDER BY player_id ASC
            """.trimIndent()

        return handle
            .createQuery(sql)
            .bind("roundId", roundId)
            .map { rs, _ ->
                Pair(
                    rs.getInt("player_id"),
                    rs.getString("dice_faces") ?: "",
                )
            }.list()
    }
}
