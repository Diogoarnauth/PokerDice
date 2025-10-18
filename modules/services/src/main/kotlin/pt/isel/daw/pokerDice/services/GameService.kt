package pt.isel.daw.pokerDice.services

import jakarta.inject.Named
import pt.isel.daw.pokerDice.domain.games.Game
import pt.isel.daw.pokerDice.domain.games.GameDomain
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

typealias GameCreationResult = Either<GameCreationError, Int?> // UUID string

sealed class GameGetByIdError {
    data object GameNotFound : GameGetByIdError()
}

typealias GameGetByIdResult = Either<GameGetByIdError, Game>

@Named
class GameService(
    private val transactionManager: TransactionManager,
    private val gameDomain: GameDomain,
    private val lobbiesDomain: LobbiesDomain,
) {
    fun createGame(
        userId: Int,
        lobbyId: Int,
    ): GameCreationResult =
        transactionManager.run {
            val existingGame = it.gamesRepository.getGameByLobbyId(lobbyId)
            val allUsersInLobby = it.usersRepository.getAllUsersInLobby(lobbyId)

            val lobby =
                it.lobbiesRepository.getById(lobbyId)
                    ?: return@run failure(GameCreationError.LobbyNotFound(lobbyId))

            // O USER É O HOST DA LOBBY, LOGADO NA SESSÃO
            require(lobby.hostId == userId) {
                "Only the host user can start the game"
            }

            // Verificar se já existe um jogo em andamento neste lobby

            // debug before credits verification
            val usersWithCredit = allUsersInLobby.count { it.credit >= lobby.minCreditToParticipate }

            // VER SE TODOS OS USERS TÊM CREDITOS PARA COMEÇAR O GAME
            require(
                allUsersInLobby.count {
                    it.credit <= lobby.minCreditToParticipate
                } != allUsersInLobby.count(),
            ) {
                "Not all users have the necessary credits to be in the lobby."
                // TODO("ESPECIFICAR QUAL O USER QUE NÃO TEM OS CRÉDITOS
                //   + return@run failure(GameCreationError.InsufficientCredits)'")
            }

            // MIN USERS DO GAME
            require(allUsersInLobby.count() >= lobby.minUsers) {
                return@run failure(GameCreationError.NotEnoughPlayers)
            }

            if (existingGame != null) return@run failure(GameCreationError.GameAlreadyRunning)

            // TODO CHAMAR ROUNDREPOSITORY.CREATEROUND

            // update do isRunning no lobby
            it.lobbiesRepository.markGameAsStartedInLobby(lobbyId)

            val newGame = gameDomain.createGameFromLobby(lobby, allUsersInLobby.count())
            val gameId = it.gamesRepository.createGame(newGame)

            success(gameId)
        }

    fun rollDice(
        gameId: Int,
        userId: Int,
    ): Either<GameGetByIdError, List<Int>> =
        transactionManager.run {
            val game = it.gamesRepository.getGameById(gameId)
            require(game?.state == Game.GameStatus.RUNNING) {
                "Game is not currently running"
            }

            val round = roundsRepository.getCurrentRoundByGameId(gameId)
            require(round != null && round.roundOver == false) {
                "No active round found for this game"
            }

            val diceRolls = gameDomain.rollDice(game, userId)
            it.gamesRepository.updateGameDiceRolls(gameId, userId, diceRolls)

            success(diceRolls)
        }

    fun rerollDice(
        gameId: Int,
        userId: Int,
        diceIndexes: List<Int>,
        // A K J Q 10     -> 1 , 2
    ): Either<GameGetByIdError, List<Int>> =
        transactionManager.run {
            val game = it.gamesRepository.getGameById(gameId)
            require(game?.state == Game.GameStatus.RUNNING) {
                "Game is not currently running"
            }

            val round = roundsRepository.getCurrentRoundByGameId(gameId)
            require(round != null && round.roundOver == false) {
                "No active round found for this game"
            }

            val newDiceRolls = gameDomain.rerollDice(game, userId, diceIndexes)
            it.gamesRepository.updateGameDiceRolls(gameId, userId, newDiceRolls)

            success(newDiceRolls)
        }

    fun getById(id: Int): GameGetByIdResult =
        transactionManager.run {
            val game =
                it.gamesRepository.getGameById(id)
                    ?: return@run failure(GameGetByIdError.GameNotFound)
            success(game)
        }

    fun endGame(gameId: Int) =
        transactionManager.run {
            val game = it.gamesRepository.getGameById(gameId) ?: return@run
            it.gamesRepository.updateGameState(gameId, Game.GameStatus.FINISHED)
            lobbiesDomain.markLobbyAsAvailable(game.lobbyId)
        }
}
