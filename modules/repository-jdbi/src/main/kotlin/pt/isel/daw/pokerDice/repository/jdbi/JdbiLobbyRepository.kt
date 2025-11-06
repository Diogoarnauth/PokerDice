package pt.isel.daw.pokerDice.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.daw.pokerDice.domain.lobbies.Lobby
import pt.isel.daw.pokerDice.repository.LobbiesRepository

class JdbiLobbyRepository(
    private val handle: Handle,
) : LobbiesRepository {
    override fun existsByHost(hostId: Int): Boolean =
        handle
            .createQuery("SELECT COUNT(*) FROM dbo.Lobby WHERE host_id = :hostId")
            .bind("hostId", hostId)
            .mapTo<Int>()
            .single() > 0

    override fun createLobby(
        hostId: Int,
        name: String,
        description: String,
        minPlayers: Int,
        maxPlayers: Int,
        rounds: Int,
        minCreditToParticipate: Int,
    ): Int? {
        val sql = """
        INSERT INTO dbo.Lobby (
            name, 
            description, 
            host_id, 
            minPlayers, 
            maxPlayers, 
            rounds, 
            min_credit_to_participate
        ) VALUES (
            :name, 
            :description, 
            :hostId, 
            :minPlayers, 
            :maxPlayers, 
            :rounds, 
            :minCreditToParticipate
        )
        RETURNING id
    """

        return handle
            .createUpdate(sql)
            .bind("name", name)
            .bind("description", description)
            .bind("hostId", hostId)
            .bind("minPlayers", minPlayers)
            .bind("maxPlayers", maxPlayers)
            .bind("rounds", rounds)
            .bind("minCreditToParticipate", minCreditToParticipate)
            .executeAndReturnGeneratedKeys("id")
            .mapTo<Int>()
            .singleOrNull()
    }

    override fun deleteLobbyById(lobbyId: Int) {
        handle
            .createUpdate("DELETE FROM dbo.Lobby WHERE id = :id")
            .bind("id", lobbyId)
            .execute()
    }

    override fun markGameAsStartedInLobby(lobbyId: Int) {
        handle
            .createUpdate("UPDATE dbo.Lobby SET isRunning = TRUE WHERE id = :lobbyId")
            .bind("lobbyId", lobbyId)
            .execute()
    }

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

    override fun getLobbiesNotFull(): List<Lobby> =
        handle
            .createQuery(
                """
            SELECT 
                l.id, 
                l.name, 
                l.description, 
                l.minPlayers, 
                l.maxPlayers, 
                l.rounds, 
                l.min_credit_to_participate,
                COUNT(p.id) AS current_players
            FROM dbo.Lobby l
            LEFT JOIN dbo.Users p ON l.id = p.lobby_id
            GROUP BY 
                l.id, 
                l.name, 
                l.description, 
                l.minPlayers, 
                l.maxPlayers, 
                l.rounds, 
                l.min_credit_to_participate
            HAVING COUNT(p.id) < l.maxPlayers
            """,
            ).map { rs, _ ->
                Lobby(
                    id = rs.getInt("id"),
                    name = rs.getString("name"),
                    description = rs.getString("description"),
                    hostId = 0,
                    // não existe host_id na tabela atual
                    minUsers = rs.getInt("minPlayers"),
                    maxUsers = rs.getInt("maxPlayers"),
                    rounds = rs.getInt("rounds"),
                    minCreditToParticipate = rs.getInt("min_credit_to_participate"),
                    isRunning = false,
                    // valor default
                )
            }.list()

    override fun markLobbyAsAvailable(lobbyId: Int) {
        val rows =
            handle
                .createUpdate(
                    """
                    UPDATE dbo.lobby
                    SET state = 'AVAILABLE'
                    WHERE id = :id
                    """.trimIndent(),
                ).bind("id", lobbyId)
                .execute()

        require(rows == 1) { "Lobby not found or not updated (id=$lobbyId)" }
    }

    override fun getById(id: Int): Lobby? {
        val sql = """
        SELECT *
        FROM dbo.lobby l
        WHERE l.id = :id
    """

        return handle
            .createQuery(sql)
            .bind("id", id)
            .map { rs, _ ->
                Lobby(
                    id = rs.getInt("id"),
                    name = rs.getString("name"),
                    description = rs.getString("description"),
                    hostId = rs.getInt("host_id"),
                    minUsers = rs.getInt("minPlayers"),
                    maxUsers = rs.getInt("maxPlayers"),
                    rounds = rs.getInt("rounds"),
                    minCreditToParticipate = rs.getInt("min_credit_to_participate"),
                    isRunning = false,
                )
            }.singleOrNull()
    }
}

// Classe auxiliar interna para mapear o resultado SQL
private data class LobbyRow(
    val id: Int,
    val name: String,
    val description: String,
    val host_id: Int,
    val min_players: Int,
    val max_players: Int,
    val rounds: Int,
    val min_credit_to_participate: Int,
    val current_players: Int,
) {
    fun toLobby() =
        Lobby(
            id = id,
            name = name,
            description = description,
            hostId = host_id,
            minUsers = min_players,
            maxUsers = max_players,
            rounds = rounds,
            minCreditToParticipate = min_credit_to_participate,
        )
}
