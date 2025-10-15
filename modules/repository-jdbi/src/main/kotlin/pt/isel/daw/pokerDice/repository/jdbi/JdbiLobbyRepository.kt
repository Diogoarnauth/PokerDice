package pt.isel.daw.pokerDice.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.daw.pokerDice.domain.lobbies.Lobby
import pt.isel.daw.pokerDice.domain.users.PasswordValidationInfo
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
        isPrivate: Boolean,
        passwordValidationInfo: PasswordValidationInfo?,
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
            is_private, 
            password_validation, 
            minPlayers, 
            maxPlayers, 
            rounds, 
            min_credit_to_participate
        ) VALUES (
            :name, 
            :description, 
            :hostId, 
            :isPrivate, 
            :passwordValidation, 
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
            .bind("isPrivate", isPrivate)
            .bind("passwordValidation", passwordValidationInfo?.validationInfo) // null se público
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

    override fun getLobbiesNotFull(): List<Lobby> {
        val sql = """
            SELECT l.id, l.name, l.description, l.host_id, l.is_private, 
                   l.password_validation, l.min_players, l.max_players, 
                   l.rounds, l.min_credit_to_participate,
                   COUNT(p.id) AS current_players
            FROM dbo.Lobby l
            LEFT JOIN dbo.Users p ON l.id = p.lobby_id
            GROUP BY l.id, l.name, l.description, l.host_id, l.is_private, 
                     l.password_validation, l.min_players, l.max_players, 
                     l.rounds, l.min_credit_to_participate
            HAVING COUNT(p.id) < l.max_players
        """

        return handle
            .createQuery(sql)
            .mapTo<LobbyRow>()
            .map { it.toLobby() }
            .list()
    }

    override fun getById(id: Int): Lobby? {
        val sql = """
        SELECT l.id, l.name, l.description, l.host_id, l.is_private, 
               l.password_validation, l.min_players, l.max_players, 
               l.rounds, l.min_credit_to_participate
        FROM dbo.Lobby l
        WHERE l.id = :id
    """

        return handle
            .createQuery(sql)
            .bind("id", id)
            .mapTo<LobbyRow>()
            .singleOrNull()
            ?.toLobby()
    }
}

// 🔹 Classe auxiliar interna para mapear o resultado SQL
private data class LobbyRow(
    val id: Int,
    val name: String,
    val description: String,
    val host_id: Int,
    val is_private: Boolean,
    val password_validation: String?,
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
            isPrivate = is_private,
            passwordValidationInfo =
                if (is_private && !password_validation.isNullOrBlank()) {
                    PasswordValidationInfo(password_validation)
                } else {
                    null
                },
            minUsers = min_players,
            maxUsers = max_players,
            rounds = rounds,
            minCreditToParticipate = min_credit_to_participate,
        )
}
