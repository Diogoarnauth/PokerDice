package pt.isel.daw.pokerDice.services

import org.springframework.stereotype.Service
import pt.isel.daw.pokerDice.domain.games.Round
import pt.isel.daw.pokerDice.repository.TransactionManager
import pt.isel.daw.pokerDice.utils.Either
import pt.isel.daw.pokerDice.utils.failure
import pt.isel.daw.pokerDice.utils.success

// Erros possíveis na criação
sealed class RoundCreationError {
    data class GameNotFound(
        val gameId: Int,
    ) : RoundCreationError()

    data object InvalidRoundData : RoundCreationError()

    data object NotEnoughPlayers : RoundCreationError()

    data class LobbyNotFound(
        val lobbyId: Int,
    ) : RoundCreationError()
}

// Resultado da criação
typealias RoundCreationResult = Either<RoundCreationError, Int>

// Erros possíveis na obtenção
sealed class RoundGetByIdError {
    data object RoundNotFound : RoundGetByIdError()
}

// Resultado da obtenção
typealias RoundGetByIdResult = Either<RoundGetByIdError, Round>

@Service
class RoundService(
    private val transactionManager: TransactionManager,
) {
    fun startRound(
        userId: Int,
        gameId: Int,
    ): RoundCreationResult =
        transactionManager.run {
            println("ENTREI NO ROUND SERVICE")

            val lobbiesRepo = it.lobbiesRepository
            val gamesRepo = it.gamesRepository
            val roundsRepo = it.roundRepository
            val usersRepo = it.usersRepository

            val game =
                gamesRepo.getGameById(gameId)
                    ?: return@run failure(RoundCreationError.LobbyNotFound(gameId))

            println("game obtido? SIM com lobbyId: ${game.lobbyId}")

            val lobby =
                lobbiesRepo.getById(game.lobbyId)
                    ?: return@run failure(RoundCreationError.LobbyNotFound(game.lobbyId))

            require(lobby.hostId == userId) { "Only the host user can start the round." }
            require(game.state.name != "FINISHED") { "Cannot start a new round in a finished game." }

            val playersInLobby = usersRepo.getAllUsersInLobby(lobby.id)
            if (playersInLobby.size < lobby.minUsers) {
                return@run failure(RoundCreationError.NotEnoughPlayers)
            }

            val playersWithCredit = playersInLobby.filter { it.credit >= lobby.minCreditToParticipate }
            if (playersWithCredit.size < lobby.minUsers) {
                return@run failure(RoundCreationError.NotEnoughPlayers)
            }

            val round =
                Round(
                    id = null,
                    // A base de dados gera o SERIAL
                    gameId = gameId,
                    roundWinners = null,
                    bet = lobby.minCreditToParticipate,
                    roundOver = false,
                    roundNumber = game.roundCounter + 1,
                )

            val newRoundId = roundsRepo.createRound(gameId, round)

            gamesRepo.updateCurrentRound(gameId, newRoundId)

            success(newRoundId)
        }
}
