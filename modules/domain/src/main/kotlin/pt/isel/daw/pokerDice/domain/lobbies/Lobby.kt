package pt.isel.daw.pokerDice.domain.lobbies

// Lobby interno
data class Lobby(
    val id: Int,
    val name: String,
    val description: String,
    val hostId: Int,
    // val isPrivate: Boolean,
    // val passwordValidationInfo: PasswordValidationInfo?,
    val minUsers: Int,
    val maxUsers: Int,
    val rounds: Int,
    val minCreditToParticipate: Int,
    var isRunning: Boolean = false,
)
