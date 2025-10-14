package pt.isel.daw.pokerDice.http.model.UserModel
/*
import org.example.Domain.Players.Player

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
        val players: List<Player>,
        val minPlayers: Int,
        val maxPlayers: Int,
        val rounds: Int
    ) : GetLobbyResult

    object NotFound : GetLobbyResult
}*/