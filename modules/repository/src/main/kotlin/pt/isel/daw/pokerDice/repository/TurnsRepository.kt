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

    fun getTurnsByRoundIdForAllPlayers(roundId: Int): List<Turn>

    fun getPlayersWithTurns(roundId: Int): List<Pair<Int, String>>
}
