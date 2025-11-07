package pt.isel.daw.pokerDice.services

import jakarta.inject.Named
import pt.isel.daw.pokerDice.domain.games.Dice
import pt.isel.daw.pokerDice.domain.games.Game
import pt.isel.daw.pokerDice.domain.games.GameDomain
import pt.isel.daw.pokerDice.domain.games.Round
import pt.isel.daw.pokerDice.domain.games.Turn
import pt.isel.daw.pokerDice.domain.lobbies.LobbiesDomain
import pt.isel.daw.pokerDice.domain.lobbies.Lobby
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

    data object NotTheHost : GameCreationError()
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

    data object NotFirstRoll : GameError()

    data object LobbyDontFoundByGame : GameError()

    data class NoActiveRound(
        val gameId: Int,
    ) : GameError()

    data class NoActiveTurn(
        val roundId: Int,
    ) : GameError()

    data class InvalidDiceIndexes(
        val indexes: List<Int>,
    ) : GameError()

    object IsNotYouTurn : GameError()

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

            require(lobby.hostId == userId) {
                "Only the host user can start the game"
            }

            if (existingGame != null) return@run failure(GameCreationError.GameAlreadyRunning)

            val usersWithoutCredit = allUsersInLobby.filter { it.credit < lobby.minCreditToParticipate }
            require(usersWithoutCredit.isEmpty()) {
                "Not all users have the necessary credits to participate"
            }

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

            // println("Jogo criado com ID: $gameId")

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
            //  println("Round inicial criado com ID: $roundId")

            // Criar o primeiro turno â€” comeÃ§a o primeiro jogador da lista
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
            println("1")
            val curGame =
                it.gamesRepository.getGameByLobbyId(lobbyId)
                    ?: return@run failure(GameError.GameNotFound(lobbyId))
            println("2")
            val curRound =
                it.roundRepository.getRoundsByGameId(curGame.id!!).first { it -> !it.roundOver }
            println("3")
            val curTurn =
                it.turnsRepository.getTurnsByRoundId(curRound.id!!) // SÃ“ DEVE HAVER 1

            if (curTurn.playerId != userId) {
                println("entrou aqui")
                return@run failure(GameError.IsNotYouTurn)
            }
            println("4")
            if (curTurn.rollCount != 0) return@run failure(GameError.NotFirstRoll)
            println("5")
            if (curTurn.isDone) {
                return@run failure(GameError.TurnAlreadyFinished)
            }
            println("6")
            println("userId $userId")
            println("curTurn.playerId ${curTurn.playerId}")

            println("7")
            val rolledDice = gameDomain.rollDice(curTurn)

            val newRollCount = curTurn.rollCount + 1
            // val isTurnFinished = newRollCount >= 3 escusado

            // isTurnFinished, sempre false
            it.turnsRepository.updateTurn(
                turnId = curTurn.id!!,
                rollCount = newRollCount,
                diceResults = rolledDice,
                value_of_combination = curTurn.value_of_combination,
                isDone = false,
            )

            // println(" CHEGUEI ANTES DE PASSAR PARA O PRÃ“XIMO USER")

            /* if (isTurnFinished) {
                 val nextPlayerId = it.turnsRepository.getNextPlayerInRound(curRound.id!!, lobbyId, curTurn.playerId!!)
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
                     // todos os jogadores jÃ¡ jogaram â†’ marcar round como terminado
                     it.roundRepository.markRoundAsOver(curRound.id!!)
                 }
             }*/

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

            val curTurn = it.turnsRepository.getTurnsByRoundId(round.id!!)

            if (curTurn.playerId != userId) {
                return@run failure(GameError.IsNotYouTurn)
            }

            if (curTurn.isDone) {
                return@run failure(GameError.TurnAlreadyFinished)
            }

            if (curTurn.rollCount >= 3) {
                endTurn(game.id!!, userId)
            }

            val updatedDice = gameDomain.rerollDice(curTurn, keepIndexes)

            val newRollCount = curTurn.rollCount + 1
            val isDone = newRollCount > 3
            it.turnsRepository.updateTurn(
                turnId = curTurn.id!!,
                rollCount = newRollCount,
                diceResults = updatedDice,
                value_of_combination = curTurn.value_of_combination,
                isDone = isDone,
            )

            if (isDone) {
                val nextPlayerId = it.turnsRepository.getNextPlayerInRound(round.id!!, lobbyId, curTurn.playerId!!)
                if (nextPlayerId != null) {
                    val nextTurn =
                        Turn(
                            id = null,
                            roundId = round.id!!,
                            playerId = nextPlayerId,
                            rollCount = 0,
                            isDone = false,
                        )
                    it.turnsRepository.createTurn(round.id!!, nextTurn)
                } else {
                    // todos os jogadores jÃ¡ jogaram â†’ marcar round como terminado
                    it.roundRepository.markRoundAsOver(round.id!!)
                }
            }
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

            val winners = it.roundRepository.getGameWinner(game.id!!) // apenas calcula
            println("Game ${game.id} ended. Winners: $winners")

            if (winners.isNotEmpty()) {
                it.gamesRepository.addGameWinners(game.id!!, winners) // aqui sim grava
            }

            it.lobbiesRepository.markLobbyAsAvailable(game.lobbyId)
        }

    fun endTurn(
        gameId: Int,
        userId: Int,
    ): GameErrorResult =
        transactionManager.run {
            val game =
                it.gamesRepository.getGameById(gameId)
                    ?: return@run failure(GameError.GameNotFound(gameId))
            val lobby =
                it.lobbiesRepository.getById(game.lobbyId)
                    ?: return@run failure(GameError.LobbyDontFoundByGame) // impossivel se metermos delete cascade

            val currentRound =
                it.roundRepository.getRoundsByGameId(game.id!!).firstOrNull { r -> !r.roundOver }
                    ?: return@run failure(GameError.NoActiveRound(game.id!!))

            val curTurn =
                it.turnsRepository.getTurnsByRoundId(currentRound.id!!)
                    ?: return@run failure(GameError.NoActiveTurn(currentRound.id!!))

            if (curTurn.playerId != userId) {
                return@run failure(GameError.IsNotYouTurn)
            }

            val diceList: List<Dice> =
                curTurn.diceFaces
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.map { face ->
                        when (face.uppercase()) {
                            "A" -> Dice.Ace
                            "K" -> Dice.King
                            "Q" -> Dice.Queen
                            "J" -> Dice.Jack
                            "10" -> Dice.Ten
                            "9" -> Dice.Nine
                            else -> error("Invalid dice face: $face")
                        }
                    }
                    ?: emptyList()

            // Marca o turno atual como terminado
            it.turnsRepository.updateTurn(
                turnId = curTurn.id!!,
                rollCount = curTurn.rollCount,
                diceResults = curTurn.diceFaces ?: "",
                value_of_combination = gameDomain.score(diceList).toInt(),
                isDone = true,
            )

            val totalTurns = it.turnsRepository.getAllTurnsByRoundId(currentRound.id!!)
            val totalPlayers = it.usersRepository.getAllUsersInLobby(lobby.id).size

            if (totalTurns >= totalPlayers) {
                endRound(game, lobby, currentRound)
            } else {
                // TODO() otimizar isto
                val nextPlayerId =
                    it.turnsRepository.getNextPlayerInRound(currentRound.id!!, game.lobbyId, curTurn.playerId!!)

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
                    // terminar ronda e começar nova
                    endRound(game, lobby, currentRound)
                }
                success("Turno terminado com sucesso.")
            }
        }

    fun endRound(
        game: Game,
        lobby: Lobby,
        currentRound: Round,
    ): GameErrorResult =
        transactionManager.run {
            println("ENTREI NO ENDROUND")

            it.roundRepository.markRoundAsOver(currentRound.id!!)
            it.gamesRepository.updateRoundCounter(game.id!!)

            val winners = it.turnsRepository.getBiggestValue(currentRound.id!!) // agora List<Turn>

            println("Round ${currentRound.roundNumber} terminado. Winners: ${winners.map { it.playerId }}")

            it.roundRepository.attributeWinnerStatus(
                currentRound.id!!,
                winners.map { it.playerId },
            )

            println("game.roundCounter++ ${game.roundCounter++}")
            println("lobby.rounds ${lobby.rounds}")

            if (game.roundCounter++ >= lobby.rounds) {
                println("AHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH")
                endGame(game.id!!)
                success("Game Finished")
            } else {
                val nextRound =
                    Round(
                        id = null,
                        roundNumber = currentRound.roundNumber + 1,
                        gameId = game.id!!,
                        bet = currentRound.bet,
                        roundOver = false,
                    )
                val nextRoundId = it.roundRepository.createRound(game.id!!, nextRound)

                val firstPlayer = it.usersRepository.getAllUsersInLobby(game.lobbyId).first()
                val firstTurn =
                    Turn(
                        id = null,
                        roundId = nextRoundId,
                        playerId = firstPlayer.id,
                        rollCount = 0,
                        isDone = false,
                    )
                it.turnsRepository.createTurn(nextRoundId, firstTurn)

                success("round ended with success")
            }
        }
}
