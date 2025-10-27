package pt.isel.daw.pokerDice.repository

import pt.isel.daw.pokerDice.domain.games.Game

interface GamesRepository {
    fun createGame(game: Game): Int?

    fun getGameById(id: Int): Game?

    fun getGameByLobbyId(lobbyId: Int): Game?

    fun updateGameState(
        gameId: Int,
        state: Game.GameStatus,
    )

    fun updateRoundCounter(gameId: Int)
}
