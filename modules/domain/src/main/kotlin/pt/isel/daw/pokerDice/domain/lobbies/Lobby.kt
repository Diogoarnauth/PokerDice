package pt.isel.daw.pokerDice.domain.lobbies

import pt.isel.daw.pokerDice.domain.players.PasswordValidationInfo

// Lobby interno
data class Lobby(
    val id: Int,
    val name: String,
    val description: String,
    val hostId: Int,
    val isPrivate: Boolean,
    val passwordValidationInfo: PasswordValidationInfo?,
    val minPlayers: Int,
    val maxPlayers: Int,
    val rounds: Int,
    val minCreditToParticipate: Int,

)
