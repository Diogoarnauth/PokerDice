package pt.isel.daw.pokerDice.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import org.postgresql.util.PGInterval
import pt.isel.daw.pokerDice.domain.lobbies.Lobby
import pt.isel.daw.pokerDice.repository.LobbiesRepository
import java.time.Duration

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
        turn_time: Duration,
    ): Int? {
        val sql = """
    INSERT INTO dbo.Lobby (
        name, 
        description, 
        host_id, 
        minPlayers, 
        maxPlayers, 
        rounds, 
        min_credit_to_participate,
        turn_time
    ) VALUES (
        :name, 
        :description, 
        :hostId, 
        :minPlayers, 
        :maxPlayers, 
        :rounds, 
        :minCreditToParticipate,
        :turn_time
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
            .bind("turn_time", turn_time)
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

    override fun getLobbiesNotFull(): List<Lobby> =
        handle
            .createQuery(
                """
            SELECT 
                l.id,
                l.name,
                l.description,
                l.host_id,
                l.minPlayers,
                l.maxPlayers,
                l.rounds,
                l.min_credit_to_participate,
                l.turn_time,
                COUNT(p.id) AS current_players
            FROM dbo.lobby l
            LEFT JOIN dbo.users p ON l.id = p.lobby_id
            GROUP BY 
                l.id,
                l.name,
                l.description,
                l.host_id,
                l.minPlayers,
                l.maxPlayers,
                l.rounds,
                l.min_credit_to_participate,
                l.turn_time
            HAVING COUNT(p.id) < l.maxPlayers
            """,
            ).map { rs, _ ->
                Lobby(
                    id = rs.getInt("id"),
                    name = rs.getString("name"),
                    description = rs.getString("description"),
                    hostId = rs.getInt("host_id"),
                    minUsers = rs.getInt("minPlayers"),
                    maxUsers = rs.getInt("maxPlayers"),
                    rounds = rs.getInt("rounds"),
                    minCreditToParticipate = rs.getInt("min_credit_to_participate"),
                    turnTime =
                        rs.getObject("turn_time", PGInterval::class.java).let {
                            Duration.ofSeconds(it.seconds.toLong() + it.minutes * 60L + it.hours * 3600L)
                        },
                    isRunning = false,
                )
            }.list()

    override fun markLobbyAsAvailable(lobbyId: Int) {
        val rows =
            handle
                .createUpdate(
                    """
                    UPDATE dbo.lobby
                    SET isRunning = FALSE
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
                val turnTimeString = rs.getString("turn_time") // Obtém o intervalo como string

                // Tenta converter o intervalo em uma Duration
                val turnTime = convertToDuration(turnTimeString)

                Lobby(
                    id = rs.getInt("id"),
                    name = rs.getString("name"),
                    description = rs.getString("description"),
                    hostId = rs.getInt("host_id"),
                    minUsers = rs.getInt("minPlayers"),
                    maxUsers = rs.getInt("maxPlayers"),
                    rounds = rs.getInt("rounds"),
                    minCreditToParticipate = rs.getInt("min_credit_to_participate"),
                    turnTime = turnTime,
                    isRunning = false,
                )
            }.singleOrNull()
    }

    // Função para converter o intervalo (como string) em Duration
    private fun convertToDuration(turnTimeString: String?): Duration {
        println("<REPO> turnTimeString: $turnTimeString")
        if (turnTimeString == null) {
            println("<REPO> turnTimeString is null, returning Duration.ZERO")
            return Duration.ZERO
        }

        // Regex para capturar minutos no formato HH:mm:ss
        val regex = """^00:(\d{1,2}):00$""".toRegex()
        println("<REPO> regex pattern: $regex")

        // Tenta encontrar a correspondência com o regex
        val matchResult = regex.find(turnTimeString)
        println("<REPO> matchResult: $matchResult")

        return if (matchResult != null) {
            val minutes = matchResult.groupValues[1].toInt() // Captura os minutos
            println("<REPO> Found minutes: $minutes")

            // Cria a Duration com os minutos
            Duration.ofMinutes(minutes.toLong())
        } else {
            println("<REPO> No match found, returning Duration.ZERO")
            Duration.ZERO // Retorna Duration.ZERO se o formato não for reconhecido
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
        val turn_time: Duration,
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
                turnTime = turn_time,
            )
    }
}
