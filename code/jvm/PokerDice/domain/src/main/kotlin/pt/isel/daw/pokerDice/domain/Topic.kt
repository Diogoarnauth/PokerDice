package pt.isel.daw.pokerDice.domain

sealed class Topic(
    val value: String,
) {
    // Página da lista de lobbies
    data object Lobbies : Topic("lobbies")

    // TODO() Implementar mais depois

    // Um lobby específico (ex: lobby 5)
    // data class Lobby(val lobbyId: Int) : Topic("lobby:$lobbyId")

    // Um jogo específico (ex: game 12)
    // data class Game(val gameId: Int) : Topic("game:$gameId")

    // Página de perfil
    // data object Profile : Topic("profile")

    // Página inicial
    data object Home : Topic("home")
}
