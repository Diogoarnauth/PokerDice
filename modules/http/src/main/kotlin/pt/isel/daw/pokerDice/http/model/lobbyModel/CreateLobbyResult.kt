package pt.isel.daw.pokerDice.http.model.lobbyModel

/**
 * usado pelo service para indicar se a criação do lobby foi bem-sucedida
 * Ok- devolve o ID do lobby criado
 * InvalidSettings cobre casos de configuração inválida (ex: numero de jogadores incoerente )
 */

sealed interface CreateLobbyResult {
    data class Ok(val lobbyId: Int) : CreateLobbyResult

    object InvalidSettings : CreateLobbyResult
}
