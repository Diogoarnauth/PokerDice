package pt.isel.daw.pokerDice.services

import jakarta.inject.Named
import pt.isel.daw.pokerDice.domain.games.Game
import pt.isel.daw.pokerDice.domain.games.GameDomain
import pt.isel.daw.pokerDice.domain.games.Round
import pt.isel.daw.pokerDice.domain.games.Turn
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

            // Só o host pode começar o jogo
            require(lobby.hostId == userId) {
                "Only the host user can start the game"
            }

            // Verifica se já existe jogo em andamento
            if (existingGame != null) return@run failure(GameCreationError.GameAlreadyRunning)

            // Verificar créditos mínimos
            val usersWithoutCredit = allUsersInLobby.filter { it.credit < lobby.minCreditToParticipate }
            require(usersWithoutCredit.isEmpty()) {
                "Not all users have the necessary credits to participate"
            }

            // Verificar número mínimo de jogadores
            if (allUsersInLobby.count() < lobby.minUsers) {
                return@run failure(GameCreationError.NotEnoughPlayers)
            }

            // Marcar lobby como em jogo
            it.lobbiesRepository.markGameAsStartedInLobby(lobbyId)

            // Criar o jogo
            val newGame = gameDomain.createGameFromLobby(lobby, allUsersInLobby.count())
            val gameId =
                it.gamesRepository.createGame(newGame)
                    ?: return@run failure(GameCreationError.GameAlreadyRunning)

            println("Jogo criado com ID: $gameId")

            // Criar a primeira ronda
            val roundToCreate =
                Round(
                    id = null,
                    roundNumber = 1,
                    gameId = gameId,
                    bet = lobby.minCreditToParticipate,
                    roundOver = false,
                )

            val roundId = it.roundRepository.createRound(gameId, roundToCreate)
            println("Round inicial criado com ID: $roundId")

            // Criar o primeiro turno — começa o primeiro jogador da lista
            val turnToCreate =
                Turn(
                    id = null,
                    roundId = roundId,
                    playerId = allUsersInLobby.first().id,
                    rollCount = 0,
                    isDone = false,
                )

            it.turnsRepository.createTurn(roundId, turnToCreate)
            println(" Primeiro turno criado para jogador: ${allUsersInLobby.first().id}")

            success(gameId)
        }

    fun rollDice(
        gameId: Int,
        userId: Int,
    ): Either<GameGetByIdError, List<Int>> = TODO()
    /*
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

     */

    fun rerollDice(
        gameId: Int,
        userId: Int,
        diceIndexes: List<Int>,
        // A K J Q 10     -> 1 , 2
    ): Either<GameGetByIdError, List<Int>> =
        TODO()

        /*
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

         */

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
            it.gamesRepository.updateGameState(gameId, Game.GameStatus.CLOSED)
            lobbiesDomain.markLobbyAsAvailable(game.lobbyId)
        }

    fun endTurn(
        gameId: Int,
        userId: Int,
    ) {
        TODO()
    }
}
