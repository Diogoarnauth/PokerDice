package pt.isel.daw.pokerDice.repository

import pt.isel.daw.pokerDice.domain.games.Round

interface RoundRepository {
    fun createRound(round: Round): Int

    fun getRoundById(roundId: Int): Round?

    fun updateRound(round: Round)
}
