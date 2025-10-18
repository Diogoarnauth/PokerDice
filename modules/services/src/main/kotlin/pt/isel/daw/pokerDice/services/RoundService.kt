package pt.isel.daw.pokerDice.services

import org.springframework.stereotype.Service
import pt.isel.daw.pokerDice.domain.games.Round
import pt.isel.daw.pokerDice.repository.TransactionManager
import pt.isel.daw.pokerDice.utils.failure
import pt.isel.daw.pokerDice.utils.success

@Service
class RoundService(
    private val transactionManager: TransactionManager,
) {
    fun startRound(
        userId: Int,
        gameId: Int,
    ): GameCreationResult =
        transactionManager.run {
            println("ENTREI NO ROUND SERVICE")

            val lobbiesRepo = it.lobbiesRepository
            val gamesRepo = it.gamesRepository
            val roundsRepo = it.roundRepository
            val usersRepo = it.usersRepository

            val game =
                gamesRepo.getGameById(gameId)
                    ?: return@run failure(GameCreationError.LobbyNotFound(gameId))

            println("game obtido? SIM com lobbyId: ${game.lobbyId}")

            val lobby =
                lobbiesRepo.getById(game.lobbyId)
                    ?: return@run failure(GameCreationError.LobbyNotFound(game.lobbyId))

            require(lobby.hostId == userId) { "Only the host user can start the round." }
            require(game.state.name != "FINISHED") { "Cannot start a new round in a finished game." }

            val playersInLobby = usersRepo.getAllUsersInLobby(lobby.id)
            if (playersInLobby.size < lobby.minUsers) {
                return@run failure(GameCreationError.NotEnoughPlayers)
            }

            val playersWithCredit = playersInLobby.filter { it.credit >= lobby.minCreditToParticipate }
            if (playersWithCredit.size < lobby.minUsers) {
                return@run failure(GameCreationError.NotEnoughPlayers)
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

            // ✅ atualiza o jogo para apontar para a nova ronda
            gamesRepo.updateCurrentRound(gameId, newRoundId)

            success(newRoundId)
        }
}
