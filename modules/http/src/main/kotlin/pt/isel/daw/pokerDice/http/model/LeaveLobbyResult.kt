package pt.isel.daw.pokerDice.http.model

/**
 * Resultado de DELETE /lobbies/{id}/players/me (sair do lobby).
 * Ok → saiu com sucesso
 * NotFound → lobby inexistente
 * NotInLobby → jogador não estava no lobby
 */

sealed interface LeaveLobbyResult {
    object Ok : LeaveLobbyResult
    object NotFound : LeaveLobbyResult
    object NotInLobby : LeaveLobbyResult
}