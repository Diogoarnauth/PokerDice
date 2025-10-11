package pt.isel.daw.pokerDice.domain.Lobbies

import pt.isel.daw.pokerDice.domain.players.PasswordValidationInfo
import pt.isel.daw.pokerDice.domain.players.Player

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
