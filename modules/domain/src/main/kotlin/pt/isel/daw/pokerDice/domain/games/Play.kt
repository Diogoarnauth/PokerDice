package pt.isel.daw.pokerDice.domain.games

import java.time.Instant

data class Play(
    val playerId: Int,
    val dice: List<Dice>,
    val score: Int,
    val timestamp: Instant,
)
