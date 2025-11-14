package pt.isel.daw.pokerDice.repository

import pt.isel.daw.pokerDice.domain.games.Round

interface RoundRepository {
    fun createRound(
        gameId: Int,
        round: Round,
    ): Int

    fun getRoundsByGameId(gameId: Int): List<Round>

    fun markRoundAsOver(roundId: Int)

    fun attributeWinnerStatus(
        roundId: Int,
        winnerIds: List<Int>,
    )

    fun getGameWinner(gameId: Int): List<Int>
}
