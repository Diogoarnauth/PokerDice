package pt.isel.daw.pokerDice.domain.lobbies
import java.time.Duration

// Lobby interno
data class Lobby(
    val id: Int,
    val name: String,
    val description: String,
    val hostId: Int,
    val minUsers: Int,
    val maxUsers: Int,
    val rounds: Int,
    val minCreditToParticipate: Int,
    var isRunning: Boolean = false,
    val turnTime: Duration,
)
