package pt.isel.daw.pokerDice.repository

import pt.isel.daw.pokerDice.domain.games.Turn
import pt.isel.daw.pokerDice.domain.users.User

interface TurnsRepository {
    fun createTurn(
        roundId: Int,
        turn: Turn,
    ): Int

    fun updateTurn(
        turnId: Int,
        rollCount: Int,
        diceResults: String,
        value_of_combination: Int,
        isDone: Boolean,
    )

    fun getCurrentTurn(roundId: Int): Turn

    fun getTurnsByRoundId(roundId: Int): Turn

    fun getAllTurnsByRoundId(roundId: Int): Int

    fun getNextPlayerInRound(
        roundId: Int,
        lobbyId: Int,
        currTurnPlayerId: Int,
    ): Int?

    fun getBiggestValue(roundId: Int): List<Turn>

    fun getTurnsByRoundIdForAllPlayers(roundId: Int): List<Turn>

    fun getWhichPlayerTurnByRoundId(roundId: Int): User
}
