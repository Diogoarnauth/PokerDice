package pt.isel.daw.pokerDice.services

import jakarta.inject.Named
import org.slf4j.LoggerFactory
import pt.isel.daw.pokerDice.domain.PokerEvent
import pt.isel.daw.pokerDice.domain.games.Dice
import pt.isel.daw.pokerDice.domain.games.Game
import pt.isel.daw.pokerDice.domain.games.GameDomain
import pt.isel.daw.pokerDice.domain.games.Round
import pt.isel.daw.pokerDice.domain.games.Turn
import pt.isel.daw.pokerDice.domain.lobbies.LobbiesDomain
import pt.isel.daw.pokerDice.domain.lobbies.Lobby
import pt.isel.daw.pokerDice.domain.users.User
import pt.isel.daw.pokerDice.repository.TransactionManager
import pt.isel.daw.pokerDice.utils.Either
import pt.isel.daw.pokerDice.utils.Success
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

sealed class EndGameError {
    data object GameNotFound : EndGameError()

    data object GameAlreadyClosed : EndGameError()

    data object LobbyNotFound : EndGameError()

    data object YouAreNotHost : EndGameError()
}
typealias EndGameResult = Either<EndGameError, Unit> // UUID string

typealias GameCreationResult = Either<GameCreationError, Int?> // UUID string

sealed class GameGetByIdError {
    data object GameNotFound : GameGetByIdError()
}

// Mudando o tipo para retornar o jogo completo
typealias GetGameByLobby = Either<GameError, Game?>

typealias GetCurrentTurn = Either<GameError, Turn> // Agora vai retornar o objeto Game completo

typealias GetCurrentRound = Either<GameError, Round> // Agora vai retornar o objeto Game completo

typealias GameErrorResult = Either<GameError, String>

sealed class GameError {
    data class GameNotFound(
        val lobbyId: Int,
    ) : GameError()

    data object NotFirstRoll : GameError()

    data object RoundDontExists : GameError()

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
    private val eventService: PokerDiceEventService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun getGameIdByLobby(lobbyId: Int): GetGameByLobby {
        return transactionManager.run { it ->
            val game =
                it.gamesRepository.getGameByLobbyId(lobbyId)
                    ?: return@run failure(GameError.GameNotFound(lobbyId)) // Retorna erro se não encontrar o jogo
            success(game)
        }
    }

    fun createGame(
        userId: Int,
        lobbyId: Int,
    ): GameCreationResult =
        transactionManager.run { it ->

            val existingGame = it.gamesRepository.getGameByLobbyId(lobbyId)
            val allUsersInLobby = it.usersRepository.getAllUsersInLobby(lobbyId)

            val user =
                it.usersRepository.getUserById(userId)
                    ?: return@run failure(GameCreationError.NotTheHost)

            val lobby =
                it.lobbiesRepository.getById(lobbyId)
                    ?: return@run failure(GameCreationError.LobbyNotFound(lobbyId))

            if (lobby.hostId != userId) {
                return@run failure(GameCreationError.NotTheHost)
            }

            if (existingGame != null) return@run failure(GameCreationError.GameAlreadyRunning)

            // Escusado porque se já entraram no lobby quer dizer que tem o dinheiro

            /*val usersWithoutCredit = allUsersInLobby.filter { it.credit < lobby.minCreditToParticipate }
            require(usersWithoutCredit.isEmpty()) {
                "Not all users have the necessary credits to participate"
            }*/

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

            // Criar a primeira ronda
            startNewRound(null, gameId, lobby)

            val gameStartedEvent =
                PokerEvent.GameStarted(
                    lobbyId = lobbyId,
                    gameId = gameId,
                    name = lobby.name,
                    hostUsername = user.username ?: "unknown",
                    changeType = "started",
                )

            eventService.sendToAll(
                gameStartedEvent,
            )

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
                it.turnsRepository.getTurnsByRoundId(curRound.id!!)

            if (curTurn.playerId != userId) {
                return@run failure(GameError.IsNotYouTurn)
            }
            if (curTurn.rollCount != 0) return@run failure(GameError.NotFirstRoll)

            if (curTurn.isDone) {
                return@run failure(GameError.TurnAlreadyFinished)
            }

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

            val rollEvent =
                PokerEvent.GameUpdated(
                    lobbyId = curGame.lobbyId,
                    changeType = "roll_dice",
                )

            eventService.sendToAll(
                rollEvent,
            )

            success(rolledDice)
        }

    fun reRollDice(
        lobbyId: Int,
        userId: Int,
        keepIndexes: List<Int>,
    ): GameErrorResult =
        transactionManager.run {
            logger.info("ENTREI NO REROLL SERVICES LOBBYID: $lobbyId")

            val game =
                it.gamesRepository.getGameByLobbyId(lobbyId)
                    ?: return@run failure(GameError.GameNotFound(lobbyId))

            logger.info("PASSEI O 404")

            val round = it.roundRepository.getRoundsByGameId(game.id!!).last()

            val curTurn = it.turnsRepository.getTurnsByRoundId(round.id!!)

            if (curTurn.playerId != userId) {
                return@run failure(GameError.IsNotYouTurn)
            }

            keepIndexes.forEach {
                if (it > 4) return@run failure(GameError.InvalidDiceIndexes(keepIndexes))
            }

            // verify if the turn already ended
            if (curTurn.isDone) {
                return@run failure(GameError.TurnAlreadyFinished)
            }

            if (curTurn.rollCount >= 3) {
                logger.info("ENTREI NO IF")

                endTurn(game.id!!, userId)
                val combinationDices = it.turnsRepository.getTurnByTurnId(curTurn.id!!)

                val jsonString = """{"dice": ${
                    combinationDices?.diceFaces?.split(",")?.joinToString(
                        prefix = "[\"",
                        separator = "\",\"",
                        postfix = "\"]",
                    )
                }}"""
                logger.info("jsonString $jsonString")

                success(jsonString)
            } else {
                logger.info("SAI DO IF")

                val updatedDice = gameDomain.rerollDice(curTurn, keepIndexes)

                logger.info("updatedDice $updatedDice")
                logger.info("curTurn.value_of_combination $curTurn.value_of_combination")

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
                    getNextPlayerInRound(round.id!!, lobbyId, curTurn.playerId)
                }

                val jsonString = """{"dice": ${
                    updatedDice.split(",").joinToString(
                        prefix = "[\"",
                        separator = "\",\"",
                        postfix = "\"]",
                    )
                }}"""

                val reRollEvent =
                    PokerEvent.GameUpdated(
                        lobbyId = game.lobbyId,
                        changeType = "reroll_dice",
                    )

                eventService.sendToAll(
                    reRollEvent,
                )

                success(jsonString)
            }
        }

    fun getNextPlayerInRound(
        roundId: Int,
        lobbyId: Int,
        playerId: Int,
    ) = transactionManager.run {
        val nextPlayer = it.turnsRepository.getNextPlayerInRound(roundId, lobbyId, playerId)
        if (nextPlayer != null) {
            val nextTurn =
                Turn(
                    id = null,
                    roundId = roundId,
                    playerId = nextPlayer,
                    rollCount = 0,
                    isDone = false,
                )
            it.turnsRepository.createTurn(roundId, nextTurn)
        } else {
            it.roundRepository.markRoundAsOver(roundId)
        }
    }

    fun getById(id: Int): GameGetByIdResult =
        transactionManager.run {
            val game =
                it.gamesRepository.getGameById(id)
                    ?: return@run failure(GameGetByIdError.GameNotFound)
            success(game)
        }

    fun endGame(
        gameId: Int,
        userId: Int?,
        flagFromController: Boolean,
    ): Either<EndGameError, String> =
        transactionManager.run {
            val game = it.gamesRepository.getGameById(gameId) ?: return@run failure(EndGameError.GameNotFound)

            if (flagFromController) {
                val lobby = it.lobbiesRepository.getById(game.lobbyId) ?: return@run failure(EndGameError.LobbyNotFound)

                if (lobby.hostId != userId) {
                    return@run failure(EndGameError.YouAreNotHost)
                }

                if (game.state == Game.GameStatus.CLOSED) {
                    return@run failure(EndGameError.GameAlreadyClosed)
                }

                // verificar se havia algum round aberto para devolver o dinheiro
                val allRounds = it.roundRepository.getRoundsByGameId(gameId)
                for (round in allRounds) {
                    if (!round.roundOver) {
                        val allUser = it.usersRepository.getAllUsersInLobby(lobby.id)
                        allUser.forEach { user ->
                            it.usersRepository.updateUserCredit(
                                user.id,
                                user.credit + lobby.minCreditToParticipate,
                            )
                        }
                        round.id?.let { roundId -> it.roundRepository.markRoundAsOver(roundId) }
                        break
                    }
                }
            }
            it.gamesRepository.updateGameState(gameId, Game.GameStatus.CLOSED)

            val winners = it.roundRepository.getGameWinner(game.id!!) // apenas calcula
            println("Game ${game.id} ended. Winners: $winners")

            if (winners.isNotEmpty()) {
                it.gamesRepository.addGameWinners(game.id!!, winners) // aqui sim grava
            }

            winners.forEach { winner -> it.usersRepository.incrementUserWinCounter(winner) }

            it.lobbiesRepository.markLobbyAsAvailable(game.lobbyId)

            val winnerNames =
                winners.mapNotNull { playerId ->
                    it.usersRepository.getUserById(playerId)?.username
                }

            val winnersJson =
                winnerNames.joinToString(
                    prefix = "[\"",
                    separator = "\",\"",
                    postfix = "\"]",
                )

            val json =
                if (winnerNames.isNotEmpty()) {
                    """{"message": "Game ended", "winners": $winnersJson}"""
                } else {
                    """{"message": "Game ended"}"""
                }

            val gameEndedEvent =
                PokerEvent.GameUpdated(
                    lobbyId = game.lobbyId,
                    changeType = "ended",
                )

            eventService.sendToAll(
                gameEndedEvent,
            )

            val roundWinnersEvent =
                PokerEvent.WinnerAlert(
                    lobbyId = game.lobbyId,
                    winners = winnersJson,
                    changeType = "gameWinners",
                )

            eventService.sendToAll(
                roundWinnersEvent,
            )

            success(json)
        }

    fun endTurn(
        gameId: Int,
        userId: Int,
    ): GameErrorResult =
        transactionManager.run { it ->
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

            logger.info("ValueOfCombination ${gameDomain.score(diceList).toInt()}")
            logger.info("diceResults ${curTurn.diceFaces}")

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

                    val turnEndedEvent =
                        PokerEvent.GameUpdated(
                            lobbyId = game.lobbyId,
                            changeType = "turn_ended",
                        )

                    eventService.sendToAll(
                        turnEndedEvent,
                    )
                }
                val jsonString = """{"message": "Turno terminado com sucesso."}"""

                val turnEndedEvent =
                    PokerEvent.GameUpdated(
                        lobbyId = game.lobbyId,
                        changeType = "turn_ended",
                    )

                eventService.sendToAll(
                    turnEndedEvent,
                )

                success(jsonString)
            }
        }

    fun endRound(
        game: Game,
        lobby: Lobby,
        currentRound: Round,
    ): GameErrorResult =
        transactionManager.run { it ->

            it.roundRepository.markRoundAsOver(currentRound.id!!)
            it.gamesRepository.updateRoundCounter(game.id!!)
            val players = it.usersRepository.getAllUsersInLobby(game.lobbyId)

            val winners = it.turnsRepository.getBiggestValue(currentRound.id!!) // agora List<Turn>
            println("Round ${currentRound.roundNumber} terminado. Winners: ${winners.map { it.playerId }}")

            it.roundRepository.attributeWinnerStatus(
                currentRound.id!!,
                winners.map { it.playerId },
            )

            attributeWinnersCredits(winners, players, lobby)

            if (game.roundCounter++ >= lobby.rounds) {
                val endGameResp = endGame(game.id!!, null, false)

                val messageSuccessFinished =
                    if (endGameResp is Success) {
                        // 👉 aqui usamos diretamente o JSON vindo do endGame
                        endGameResp.value
                    } else {
                        """{"message": "Game Finished."}"""
                    }

                return@run success(messageSuccessFinished)
            } else {
                startNewRound(currentRound, game.id!!, lobby)

                val winnerUsernames =
                    winners.mapNotNull { turn ->
                        players.firstOrNull { it.id == turn.playerId }?.username
                    }

                val winnersJson =
                    winnerUsernames.joinToString(
                        prefix = "[\"",
                        separator = "\",\"",
                        postfix = "\"]",
                    )

                val messageSuccess =
                    """{"message": "Round ended", "winners": $winnersJson}"""

                val roundWinnersEvent =
                    PokerEvent.WinnerAlert(
                        lobbyId = game.lobbyId,
                        winners = winnersJson,
                        changeType = "roundWinners",
                    )

                eventService.sendToAll(
                    roundWinnersEvent,
                )

                success(messageSuccess)
            }
        }

    fun startNewRound(
        currentRound: Round?,
        gameId: Int,
        lobby: Lobby,
    ) = transactionManager.run {
        var round: Round

        if (currentRound == null) {
            round =
                Round(
                    id = null,
                    gameId = gameId,
                    roundNumber = 1,
                    bet = lobby.minCreditToParticipate,
                    roundOver = false,
                )
        } else {
            round =
                Round(
                    id = null,
                    gameId = gameId,
                    roundNumber = currentRound.roundNumber + 1,
                    bet = currentRound.bet,
                    roundOver = false,
                )
        }

        val players = it.usersRepository.getAllUsersInLobby(lobby.id)
        for (player in players) {
            val creditsDecremented =
                it.usersRepository.decrementCreditsFromPlayer(lobby!!.minCreditToParticipate, player.id)

            if (!creditsDecremented) {
                logger.info("ENTREI NO DECREMENTED ${player.id}")

                it.usersRepository.userExitsLobby(lobbyId = lobby.id, userId = player.id)
                val playersOnLobby = it.usersRepository.getAllUsersInLobby(lobbyId = lobby.id)

                if (lobby.hostId == player.id) {
                    // Remover todos os jogadores associados ao lobby
                    it.usersRepository.clearLobbyForAllUsers(lobby.id)

                    // Eliminar o lobby da base de dados
                    it.lobbiesRepository.deleteLobbyById(lobby.id)

                    val hostRunnedOutOfCreditsEvent =
                        PokerEvent.GameUpdated(
                            lobbyId = lobby.id,
                            changeType = "host_runned_out_of_credits",
                        )

                    eventService.sendToAll(
                        hostRunnedOutOfCreditsEvent,
                    )
                } else if (playersOnLobby.size < lobby.minUsers) {
                    logger.info("ENTREI NO ELSE IF ${player.id}")

                    endGame(gameId, null, false)
                }

                // it.usersRepository.removePlayerFromLobby(player.id)

                val messageSuccess = """{"Game ended: Not enough players remaining with credits."}"""

                return@run success(messageSuccess)
            }
        }

        val nextRoundId = it.roundRepository.createRound(gameId, round)

        val firstPlayer = it.usersRepository.getAllUsersInLobby(lobby.id).first()

        logger.info("PLAYERSSSSSS ${it.usersRepository.getAllUsersInLobby(lobby.id)}")

        val firstTurn =
            Turn(
                id = null,
                roundId = nextRoundId,
                playerId = firstPlayer.id,
                rollCount = 0,
                isDone = false,
            )
        it.turnsRepository.createTurn(nextRoundId, firstTurn)

        val messageSuccess = """{"new round started with success."}"""

        success(messageSuccess)
    }

    fun whichPlayerTurn(gameId: Int): GameErrorResult =
        transactionManager.run {
            // get the current round that is not over
            val currentRound =
                it.roundRepository
                    .getRoundsByGameId(gameId)
                    .firstOrNull { round -> !round.roundOver }
                    ?: return@run failure(GameError.NoActiveRound(gameId))

            // Get the User who should be rolling (whose turn is not done)
            val currentPlayer =
                it.turnsRepository.getWhichPlayerTurnByRoundId(currentRound.id!!)
                    ?: return@run failure(GameError.NoActiveTurn(currentRound.id!!))

            // return the player's ID (you might want to return player info, but ID suffices)

            success(currentPlayer.username)
        }

    fun getCurrentRound(gameId: Int): GetCurrentRound =
        transactionManager.run {
            logger.info("entrei no services")

            // get the current round that is not over
            val currentRound =
                it.roundRepository
                    .getRoundsByGameId(gameId)
                    .firstOrNull { round -> !round.roundOver }
                    ?: return@run failure(GameError.NoActiveRound(gameId))

            success(currentRound)
        }

    fun getCurrentTurn(gameId: Int): GetCurrentTurn =
        transactionManager.run {
            logger.info("entrei no services")

            // get the current round that is not over
            val currentRound =
                it.roundRepository
                    .getRoundsByGameId(gameId)
                    .firstOrNull { round -> !round.roundOver }
                    ?: return@run failure(GameError.NoActiveRound(gameId))
            logger.info("EXISTE ROUND")

            // Get the User who should be rolling (whose turn is not done)
            val currentTurn =
                it.turnsRepository.getCurrentTurn(currentRound.id!!)
                    ?: return@run failure(GameError.NoActiveTurn(currentRound.id!!))
            logger.info("TURN QUE PROCURO $currentTurn")

            success(currentTurn)
        }

    fun attributeWinnersCredits(
        winners: List<Turn>,
        players: List<User>,
        lobby: Lobby,
    ) = transactionManager.run {
        // Assume you have access to your handle or transaction
        val winnerIds = winners.map { it.playerId }.toSet()
        val numbPlayers = players.size

        val credit = lobby.minCreditToParticipate

        val valueToAttribute = numbPlayers * credit / winners.size

        for (winner in winnerIds) {
            val userWinner = it.usersRepository.getUserById(winner)
            // atribute credit to the player's that won the round
            it.usersRepository.updateUserCredit(winner, userWinner!!.credit + valueToAttribute)
        }
    }

    fun getAllTurnsByRound(roundId: Int) =
        transactionManager.run {
            val turns = it.turnsRepository.getAllTurnsCompleteObjectByRoundId(roundId)
            if (turns.isEmpty()) {
                return@run failure(GameError.NoActiveTurn(roundId))
            }

            success(turns)
        }
}
