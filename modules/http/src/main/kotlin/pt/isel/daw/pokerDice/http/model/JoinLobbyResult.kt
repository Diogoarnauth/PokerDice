package pt.isel.daw.pokerDice.http.model

/**
 * Resultado de POST /lobbies/{id}/players (entrar num lobby).
 * Cobre todos os cenários: sucesso, inexistente, cheio, ou jogador já presente.
 */

sealed interface JoinLobbyResult {
    object Ok : JoinLobbyResult
    object NotFound : JoinLobbyResult
    object Full : JoinLobbyResult
    object AlreadyIn : JoinLobbyResult
}