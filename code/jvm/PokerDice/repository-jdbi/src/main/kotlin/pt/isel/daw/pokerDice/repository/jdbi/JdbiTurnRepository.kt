package pt.isel.daw.pokerDice.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.daw.pokerDice.domain.games.Turn
import pt.isel.daw.pokerDice.domain.users.User
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
        value_of_combination: Int,
        isDone: Boolean,
    ) {
        handle
            .createUpdate(
                """
        UPDATE dbo.turn
        SET roll_count = :rollCount,
            dice_faces = :diceFaces,
            value_of_combination = :value_of_combination,
            is_done = :isDone
        WHERE id = :turnId
        """,
            ).bind("rollCount", rollCount)
            .bind("diceFaces", diceResults) // Pode ser uma lista; se for texto, converte para CSV ou JSON
            .bind("value_of_combination", value_of_combination)
            .bind("isDone", isDone)
            .bind("turnId", turnId)
            .execute()
    }

    override fun getBiggestValue(roundId: Int): List<Turn> =
        handle
            .createQuery(
                """
                SELECT id, round_id, player_id, roll_count, dice_faces, value_of_combination, is_done
                FROM dbo.turn
                WHERE round_id = :roundId
                  AND value_of_combination = (
                      SELECT MAX(value_of_combination)
                      FROM dbo.turn
                      WHERE round_id = :roundId
                  )
                ORDER BY id
                """.trimIndent(),
            ).bind("roundId", roundId)
            .mapTo<Turn>()
            .list()

    override fun getTurnsByRoundId(roundId: Int): Turn =
        handle
            .createQuery(
                """
                SELECT id, round_id, player_id, roll_count, dice_faces, is_done
                FROM dbo.turn
                WHERE round_id = :roundId
                  AND is_done = FALSE
                ORDER BY id ASC
                LIMIT 1
                """.trimIndent(),
            ).bind("roundId", roundId)
            .mapTo<Turn>()
            .single()

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

    override fun getAllTurnsByRoundId(roundId: Int): Int {
        val sql =
            """
            SELECT COUNT(*) 
            FROM dbo.turn
            WHERE round_id = :roundId
            """.trimIndent()

        return handle
            .createQuery(sql)
            .bind("roundId", roundId)
            .mapTo<Int>()
            .one()
    }

    override fun getAllTurnsObjectByRoundId(roundId: Int): List<Turn> {
        val sql =
            """
            SELECT id, round_id, player_id, turn_order
            FROM dbo.turn
            WHERE round_id = :roundId
            ORDER BY turn_order
            """.trimIndent()

        return handle
            .createQuery(sql)
            .bind("roundId", roundId)
            .mapTo<Turn>()
            .list()
    }

    override fun getWhichPlayerTurnByRoundId(roundId: Int): User =
        handle
            .createQuery(
                """
                SELECT u.id,
                       u.username,
                       u.passwordValidation,
                       u.name,
                       u.age,
                       u.credit,
                       u.winCounter,
                       u.lobby_id
                FROM dbo.turn t
                JOIN dbo.users u ON t.player_id = u.id
                WHERE t.round_id = :roundId 
                    AND t.is_done = false
                ORDER BY t.id
                LIMIT 1
                """.trimIndent(),
            ).bind("roundId", roundId)
            .mapTo<User>()
            .first() // or .findOne().orElse(null) if you want nullability

    override fun getCurrentTurn(roundId: Int): Turn =
        handle
            .createQuery(
                """
                SELECT t.id AS turn_id,
                       t.round_id,
                       t.player_id,
                       t.roll_count,
                       t.dice_faces,
                       t.value_of_combination,
                       t.is_done
                FROM dbo.turn t
                WHERE t.round_id = :roundId
                  AND t.is_done = false
                ORDER BY t.id
                LIMIT 1
                """.trimIndent(),
            ).bind("roundId", roundId)
            .mapTo<Turn>()
            .first()
}
