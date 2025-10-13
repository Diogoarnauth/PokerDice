package pt.isel.daw.pokerDice.http.model


data class LobbyGetByIdOutputModel(
    val id: Int,
    val name: String,
    val description: String,
    val hostId: Int,
    val isPrivate: Boolean,
    val minPlayers: Int,
    val maxPlayers: Int,
    val rounds: Int,
    val minCreditToParticipate: Int
)
