package pt.isel.daw.pokerDice.http.model.lobbyModel

import pt.isel.daw.pokerDice.domain.users.User

class CreateLobbyOutputModel(
    val id: Int,
    val owner: User,
    // trocar para authenticated player
    val name: String,
    // estava player, mudei para dar build TODO(")
    val description: String,
    val minPlayers: Int,
    val maxPlayers: Int,
    val nRounds: Int,
)
