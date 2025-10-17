import jakarta.inject.Named
import pt.isel.daw.pokerDice.domain.games.Game
import pt.isel.daw.pokerDice.domain.lobbies.LobbiesDomain
import pt.isel.daw.pokerDice.repository.TransactionManager
import pt.isel.daw.pokerDice.utils.Either
import pt.isel.daw.pokerDice.utils.failure
import pt.isel.daw.pokerDice.utils.success

sealed class GameCreationError {
    data class LobbyNotFound(
        val id: Int,
    ) : GameCreationError()

    data object GameAlreadyRunning : GameCreationError()

    data object NotEnoughPlayers : GameCreationError()
}

typealias GameCreationResult = Either<GameCreationError, Unit>

sealed class GameGetByIdError {
    data object GameNotFound : GameGetByIdError()
}

typealias GameGetByIdResult = Either<GameGetByIdError, Game>

@Named
class GameService(
    private val transactionManager: TransactionManager,
    private val gameDomain: Game,
    private val lobbiesDomain: LobbiesDomain,
) {
    fun createGame(lobbyId: Int): GameCreationResult =
        transactionManager.run {
            val lobbiesRepo = it.lobbiesRepository
            val gamesRepo = it.gamesRepository

            // verificar se o lobby existe
            val lobby =
                lobbiesRepo.getById(lobbyId)
                    ?: return@run failure(GameCreationError.LobbyNotFound(lobbyId))

            // verificar se já há jogo a decorrer neste lobby
            val existingGame = gamesRepo.getGameByLobbyId(lobbyId)
            if (existingGame != null) {
                return@run failure(GameCreationError.GameAlreadyRunning)
            }

            // verificar se há jogadores suficientes no lobby
            val currentPlayers = lobbiesRepo.countPlayersInLobby(lobbyId)
            if (currentPlayers < lobby.minUsers) {
                return@run failure(GameCreationError.NotEnoughPlayers)
            }

            // cria um novo jogo com base no lobby
            val newGame = gameDomain.createGameFromLobby(lobby, currentPlayers)
            val gameId = gamesRepo.createGame(newGame)

            success(gameId)
        }

    fun getById(id: Int): GameGetByIdResult =
        transactionManager.run {
            val gamesRepo = it.gamesRepository
            val game =
                gamesRepo.getGameById(id)
                    ?: return@run failure(GameGetByIdError.GameNotFound)

            success(game)
        }

    // ---------- Terminar um jogo ----------
    fun endGame(gameId: Int) =
        transactionManager.run {
            val gamesRepo = it.gamesRepository
            val game = gamesRepo.getGameById(gameId) ?: return@run
            gamesRepo.updateGameState(gameId, "FINISHED")

            // O lobby pode continuar ativo para novos jogos
            lobbiesDomain.markLobbyAsAvailable(game.lobbyId)
        }
}
