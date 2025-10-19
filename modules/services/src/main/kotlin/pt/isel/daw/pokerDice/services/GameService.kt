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

typealias GameErrorResult = Either<GameError, String>

sealed class GameError {
    data class GameNotFound(
        val lobbyId: Int,
    ) : GameError()

    data class NoActiveRound(
        val gameId: Int,
    ) : GameError()

    data class NoActiveTurn(
        val roundId: Int,
    ) : GameError()

    data class InvalidDiceIndexes(
        val indexes: List<Int>,
    ) : GameError()

    object TurnAlreadyFinished : GameError()

    object InvalidRollCount : GameError()
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
        lobbyId: Int,
        userId: Int,
    ): GameErrorResult =

        transactionManager.run {
            val curGame =
                it.gamesRepository.getGameByLobbyId(lobbyId)
                    ?: return@run failure(GameError.GameNotFound(lobbyId))

            val curRound =
                it.roundRepository.getRoundsByGameId(curGame.id!!).first { it -> !it.roundOver }

            val curTurn =
                it.turnsRepository.getTurnsByRoundId(curRound.id!!, userId) // SÓ DEVE HAVER 1

            val rolledDice = gameDomain.rollDice(curTurn)

            val newRollCount = curTurn.rollCount + 1
            val isTurnFinished = newRollCount >= 3

            it.turnsRepository.updateTurn(
                turnId = curTurn.id!!,
                rollCount = newRollCount,
                diceResults = rolledDice,
                isDone = isTurnFinished,
            )

            println(" CHEGUEI ANTES DE PASSAR PARA O PRÓXIMO USER")

            if (isTurnFinished) {
                val nextPlayerId = it.turnsRepository.getNextPlayerInRound(curRound.id!!, lobbyId)
                if (nextPlayerId != null) {
                    val nextTurn =
                        Turn(
                            id = null,
                            roundId = curRound.id!!,
                            playerId = nextPlayerId,
                            rollCount = 0,
                            isDone = false,
                        )
                    it.turnsRepository.createTurn(curRound.id!!, nextTurn)
                } else {
                    // todos os jogadores já jogaram → marcar round como terminado
                    it.roundRepository.markRoundAsOver(curRound.id!!)
                }
            }

            success(rolledDice)
        }

    fun reRollDice(
        lobbyId: Int,
        userId: Int,
        keepIndexes: List<Int>,
    ): GameErrorResult =
        transactionManager.run {
            val game =
                it.gamesRepository.getGameByLobbyId(lobbyId)
                    ?: return@run failure(GameError.GameNotFound(lobbyId))

            val round = it.roundRepository.getRoundsByGameId(game.id!!).first()

            val currentTurn = it.turnsRepository.getTurnsByRoundId(round.id!!, userId)

            val updatedDice = gameDomain.rerollDice(currentTurn, keepIndexes)

            val newRollCount = currentTurn.rollCount + 1
            val isDone = newRollCount >= 3
            it.turnsRepository.updateTurn(
                turnId = currentTurn.id!!,
                rollCount = newRollCount,
                diceResults = updatedDice,
                isDone = isDone,
            )

            success(updatedDice)
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
            it.gamesRepository.updateGameState(gameId, Game.GameStatus.CLOSED)
            lobbiesDomain.markLobbyAsAvailable(game.lobbyId)
        }

    fun endTurn(
        gameId: Int,
        userId: Int,
    ) {
        transactionManager.run {
            // 1. Obter o jogo
            val game = it.gamesRepository.getGameById(gameId) ?: return@run

            // 2. Obter a ronda ativa
            val currentRound =
                it.roundRepository.getRoundsByGameId(game.id!!).firstOrNull { r -> !r.roundOver }
                    ?: return@run // nenhuma ronda ativa, não faz nada

            // 3. Obter o turno atual do jogador
            val curTurn =
                it.turnsRepository.getTurnsByRoundId(currentRound.id!!, userId)
                    ?: return@run // turno não encontrado, talvez já tenha acabado

            // 4. Marcar o turno como concluído
            it.turnsRepository.updateTurn(
                turnId = curTurn.id!!,
                rollCount = curTurn.rollCount,
                diceResults = curTurn.diceFaces ?: "", // ou os resultados finais do turno
                isDone = true,
            )

            // 5. Verificar se há próximo jogador
            val nextPlayerId = it.turnsRepository.getNextPlayerInRound(currentRound.id!!, game.lobbyId)
            if (nextPlayerId != null) {
                val nextTurn =
                    Turn(
                        id = null,
                        roundId = currentRound.id!!,
                        playerId = nextPlayerId,
                        rollCount = 0,
                        isDone = false,
                    )
                it.turnsRepository.createTurn(currentRound.id!!, nextTurn)
            } else {
                // 6. Todos os jogadores já jogaram → marcar a ronda como terminada
                it.roundRepository.markRoundAsOver(currentRound.id!!)
            }
        }
    }
}
