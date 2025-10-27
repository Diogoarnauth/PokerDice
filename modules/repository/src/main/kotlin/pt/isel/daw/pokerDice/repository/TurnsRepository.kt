package pt.isel.daw.pokerDice.repository

import pt.isel.daw.pokerDice.domain.games.Turn

interface TurnsRepository {
    fun createTurn(
        roundId: Int,
        turn: Turn,
    ): Int

    fun updateTurn(
        turnId: Int,
        rollCount: Int,
        diceResults: String,
        value_of_combination:Int,
        isDone: Boolean,
    )

    fun getTurnsByRoundId(
        roundId: Int,
        userId: Int,
    ): Turn

    fun getNextPlayerInRound(
        roundId: Int,
        lobbyId: Int,
        currTurnPlayerId: Int,
    ): Int?

    fun getBiggestValue(
        round_id: Int,
    ): Turn?

    fun getTurnsByRoundIdForAllPlayers(roundId: Int): List<Turn>
}
