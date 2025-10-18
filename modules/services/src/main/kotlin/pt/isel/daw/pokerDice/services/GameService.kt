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

            println("DEBUG createGame: checking host -> userId=$userId, lobby.hostId=${lobby.hostId}")

            // O USER É O HOST DA LOBBY, LOGADO NA SESSÃO
            require(lobby.hostId == userId) {
                "Only the host user can start the game"
            }

            println("DEBUG createGame: checking existing game -> existingGame=$existingGame, existingGameState=${existingGame?.state}")

            // Verificar se já existe um jogo em andamento neste lobby
            require(existingGame == null || existingGame.state == Game.GameStatus.FINISHED) {
                "A game is already running in this lobby"
            }

            // debug before credits verification
            val usersWithCredit = allUsersInLobby.count { it.credit >= lobby.minCreditToParticipate }
            println(
                "DEBUG createGame: verifying credits -> usersWithCredit=$usersWithCredit," +
                    " currentPlayers=${allUsersInLobby.count()}, minCredit=${lobby.minCreditToParticipate}",
            )

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

            println(
                "DEBUG createGame: checking min users -> currentPlayers=${allUsersInLobby.count()}, " +
                    "minUsers=${lobby.minUsers}, isEmpty=${allUsersInLobby.isEmpty()}",
            )

            // MIN USERS DO GAME
            require(allUsersInLobby.count() >= lobby.minUsers) {
                return@run failure(GameCreationError.NotEnoughPlayers)
            }

            println("DEBUG createGame: final existingGame check -> existingGame=$existingGame")

            if (existingGame != null) return@run failure(GameCreationError.GameAlreadyRunning)

            val newGame = gameDomain.createGameFromLobby(lobby, allUsersInLobby.count())
            val gameId = it.gamesRepository.createGame(newGame)

            success(gameId)
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
