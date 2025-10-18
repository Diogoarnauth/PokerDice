package pt.isel.daw.pokerDice.http

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.daw.pokerDice.domain.users.AuthenticatedUser
import pt.isel.daw.pokerDice.http.model.Problem
import pt.isel.daw.pokerDice.services.GameCreationError
import pt.isel.daw.pokerDice.services.GameService
import pt.isel.daw.pokerDice.utils.Failure
import pt.isel.daw.pokerDice.utils.Success

@RestController
class GameController(
    private val gameService: GameService,
) {
    @PostMapping(GameUris.Games.START)
    fun startGame(
        @PathVariable("lobbyId") lobbyId: Int,
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
    ): ResponseEntity<*> {
        val res = gameService.createGame(authenticatedUser.user.id, lobbyId = lobbyId)
        return when (res) {
            is Success ->
                ResponseEntity
                    .status(201)
                    .header("Location", GameUris.Games.BY_ID.replace("{gameId}", res.value.toString()))
                    .build<Unit>()
            is Failure ->
                when (res.value) {
                    is GameCreationError.LobbyNotFound -> Problem.response(404, Problem.lobbyNotFound)
                    GameCreationError.GameAlreadyRunning -> Problem.response(409, Problem.gameAlreadyRunning)
                    GameCreationError.NotEnoughPlayers -> Problem.response(400, Problem.notEnoughPlayers)
                }
        }
    }

    @GetMapping(GameUris.Games.BY_ID)
    fun getById(
        @PathVariable gameId: Int,
    ): ResponseEntity<*> {
        val res = gameService.getById(gameId)
        return when (res) {
            is Success -> ResponseEntity.ok(res.value)
            is Failure -> Problem.response(404, Problem.lobbyNotFound)
        }
    }

    @PostMapping(GameUris.Games.END_TURN)
    fun endTurn(
        @PathVariable gameId: Int,
    ): ResponseEntity<*> {
        gameService.endGame(gameId)
        return ResponseEntity.ok(mapOf("message" to "Game ended"))
    }
}
