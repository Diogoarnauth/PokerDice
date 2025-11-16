package pt.isel.daw.pokerDice.http.model.lobbyModel

class LobbyCreateInputModel(
    val name: String,
    val description: String,
    // val hostId: Int
    val minUsers: Int,
    val maxUsers: Int,
    val rounds: Int,
    val minCreditToParticipate: Int,
    val turnTime: Int,
)
