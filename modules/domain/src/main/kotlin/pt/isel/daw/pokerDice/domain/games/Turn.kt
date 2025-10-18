package pt.isel.daw.pokerDice.domain.games

data class Turn(
    val id: Int,
    val roundId: Int,
    val playerId: Int,
    var rollCount: Int = 0,
    var diceFaces: String,
    var isDone: Boolean = false,
) {
    // TODO()
}
