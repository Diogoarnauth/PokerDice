package pt.isel.daw.pokerDice.repository

import pt.isel.daw.pokerDice.domain.games.Round

interface RoundRepository {
    fun createRound(
        gameId: Int,
        round: Round,
    ): Int

    fun getRoundsByGameId(
        gameId: Int,
        page: Int,
        pageSize: Int,
    ): List<Round>
}
