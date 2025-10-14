package pt.isel.daw.pokerDice.http.model.userModel

import pt.isel.daw.pokerDice.domain.users.User

/**
 * Representa o resultado da busca de um lobby.
 * Ok inclui todos os detalhes do lobby
 * NotFond é retornado quando o lobby não existe.
 */

sealed interface GetLobbyResult {
    data class Ok(
        val id: Int,
        val name: String,
        val description: String,
        val hostId: Int,
        val players: List<User>,
        // estava player, mas estava comentado TODO("")
        val minPlayers: Int,
        val maxPlayers: Int,
        val rounds: Int,
    ) : GetLobbyResult

    object NotFound : GetLobbyResult
}
