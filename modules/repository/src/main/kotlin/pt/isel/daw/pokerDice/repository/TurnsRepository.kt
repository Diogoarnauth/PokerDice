package pt.isel.daw.pokerDice.repository

import pt.isel.daw.pokerDice.domain.games.Turn

interface TurnsRepository {
    fun createTurn(
        roundId: Int,
        turn: Turn,
    ): Int

    // fun getTurnsByRoundId(roundId: Int): List<Turn>

    // fun getCurrentTurnByRoundId(roundId: Int): Turn?
}
