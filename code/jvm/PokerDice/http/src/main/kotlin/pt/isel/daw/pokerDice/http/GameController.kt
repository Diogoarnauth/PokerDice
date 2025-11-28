package pt.isel.daw.pokerDice.http

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pt.isel.daw.pokerDice.domain.users.AuthenticatedUser
import pt.isel.daw.pokerDice.http.model.Problem
import pt.isel.daw.pokerDice.services.EndGameError
import pt.isel.daw.pokerDice.services.GameCreationError
import pt.isel.daw.pokerDice.services.GameError
import pt.isel.daw.pokerDice.services.GameService
import pt.isel.daw.pokerDice.utils.Failure
import pt.isel.daw.pokerDice.utils.Success

@RestController
class GameController(
    private val gameService: GameService,
) {
    @PostMapping(Uris.Games.START)
    fun startGame(
        @PathVariable("lobbyId") lobbyId: Int,
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
    ): ResponseEntity<*> {
        val res = gameService.createGame(authenticatedUser.user.id, lobbyId = lobbyId)
        return when (res) {
            is Success -> ResponseEntity.ok(res.value.toString())
                /*ResponseEntity
                    .status(201)
                    .header("Location", Uris.Games.BY_ID.replace("{gameId}", res.value.toString()))
                    .build<Unit>()*/
            is Failure ->
                when (res.value) {
                    is GameCreationError.LobbyNotFound -> Problem.response(404, Problem.lobbyNotFound)
                    is GameCreationError.GameAlreadyRunning -> Problem.response(409, Problem.gameAlreadyRunning)
                    // 422 porque viola regra de dominio
                    is GameCreationError.NotEnoughPlayers -> Problem.response(422, Problem.notEnoughPlayers)
                    is GameCreationError.NotTheHost -> Problem.response(403, Problem.NotTheHost)
                }
        }
    }

    @GetMapping(Uris.Games.GETGAME)
    fun getGameIdByLobby(
        @PathVariable id: String,
    ): ResponseEntity<*> {
        val lobbyId =
            id.toIntOrNull()
                ?: return Problem.response(400, Problem.invalidRequestContent)

        val res = gameService.getGameIdByLobby(lobbyId)

        return when (res) {
            is Success -> {
                val game = res.value // game é o objeto completo do jogo
                ResponseEntity.ok(game) // Retorna o jogo completo com sucesso
            }

            is Failure -> {
                ResponseEntity.status(404).body("Jogo não encontrado para o lobbyId $lobbyId")
            }
        }
    }

    @PostMapping(Uris.Games.ROLL)
    fun rollDice(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
        @PathVariable lobbyId: Int,
    ): ResponseEntity<*> {
        val res = gameService.rollDice(lobbyId, authenticatedUser.user.id)
        return when (res) {
            is Success -> ResponseEntity.ok(res.value)
            is Failure -> {
                when (res.value) {
                    is GameError.NotFirstRoll -> Problem.response(403, Problem.notFirstRoll)
                    is GameError.GameNotFound -> Problem.response(404, Problem.gameNotFound)
                    is GameError.TurnAlreadyFinished -> Problem.response(409, Problem.TurnAlreadyFinished)
                    is GameError.IsNotYouTurn -> Problem.response(409, Problem.IsNotYouTurn)
                    else -> {
                        Problem.response(400, Problem.anotherError)
                    }
                }
            }
        }
    }

    @PostMapping(Uris.Games.REROLL)
    fun rerollDice(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
        @PathVariable lobbyId: Int,
        @RequestBody dicePositionsMask: List<Int>,
    ): ResponseEntity<*> {
        val res = gameService.reRollDice(lobbyId, authenticatedUser.user.id, dicePositionsMask)
        return when (res) {
            is Success -> ResponseEntity.ok(res.value)
            is Failure -> {
                when (res.value) {
                    is GameError.GameNotFound -> Problem.response(404, Problem.gameNotFound)
                    is GameError.TurnAlreadyFinished -> Problem.response(409, Problem.TurnAlreadyFinished)
                    is GameError.IsNotYouTurn -> Problem.response(409, Problem.IsNotYouTurn)
                    else -> {
                        Problem.response(400, Problem.anotherError)
                    }
                }
            }
        }
    }

    @PostMapping(Uris.Games.END_TURN)
    fun endTurn(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
        @PathVariable gameId: Int,
    ): ResponseEntity<*> {
        val res = gameService.endTurn(gameId, authenticatedUser.user.id)

        return when (res) {
            is Success -> ResponseEntity.ok(res.value)
            is Failure -> {
                when (res.value) {
                    is GameError.GameNotFound -> Problem.response(404, Problem.gameNotFound)
                    is GameError.NoActiveRound -> Problem.response(409, Problem.noActiveRound)
                    is GameError.NoActiveTurn -> Problem.response(409, Problem.noActiveTurn)
                    is GameError.IsNotYouTurn -> Problem.response(409, Problem.IsNotYouTurn)
                    else -> {
                        Problem.response(400, Problem.anotherError)
                    }
                }
            }
        }
    }

    @GetMapping(Uris.Games.BY_ID)
    fun getById(
        @PathVariable gameId: Int,
    ): ResponseEntity<*> {
        val res = gameService.getById(gameId)
        return when (res) {
            is Success -> ResponseEntity.ok(res.value)
            is Failure -> Problem.response(404, Problem.lobbyNotFound)
        }
    }

    @PostMapping(Uris.Games.END_GAME)
    fun endGame(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
        @PathVariable gameId: Int,
    ): ResponseEntity<*> {
        val res = gameService.endGame(gameId, authenticatedUser.user.id, true)
        return when (res) {
            is Success ->
                ResponseEntity.ok(mapOf("message" to "Game ended successfully"))

            is Failure ->
                when (res.value) {
                    EndGameError.GameNotFound ->
                        Problem.response(404, Problem.gameNotFound)
                    EndGameError.GameAlreadyClosed ->
                        Problem.response(409, Problem.gameAlreadyClosed)
                    EndGameError.LobbyNotFound ->
                        Problem.response(404, Problem.lobbyNotFound)
                    EndGameError.YouAreNotHost ->
                        Problem.response(409, Problem.YouAreNotHost)
                }
        }
    }

    @GetMapping(Uris.Games.PLAYER_TURN)
    fun whichPlayerTurn(
        @PathVariable gameId: Int,
    ): ResponseEntity<*> {
        val res = gameService.whichPlayerTurn(gameId)
        return when (res) {
            is Success -> ResponseEntity.ok(mapOf("username" to res.value))
            is Failure -> Problem.response(404, Problem.lobbyNotFound)
        }
    }
}
