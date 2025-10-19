package pt.isel.daw.pokerDice.repository

import pt.isel.daw.pokerDice.domain.games.Turn
import pt.isel.daw.pokerDice.domain.lobbies.Lobby

interface TurnsRepository {
    fun createTurn(
        roundId: Int,
        turn: Turn,
    ): Int

    fun updateTurn(
        turnId: Int,
        rollCount: Int,
        diceResults: String,
        isDone: Boolean,
    )

    fun getTurnsByRoundId(
        roundId: Int,
        userId: Int,
    ): Turn

    fun getNextPlayerInRound(
        roundId: Int,
        lobbyId: Int,
    ): Int?

    // fun getCurrentTurnByRoundId(roundId: Int): Turn?
}
