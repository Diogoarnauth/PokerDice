import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.daw.pokerDice.domain.users.AuthenticatedUser
import pt.isel.daw.pokerDice.http.GameUris
import pt.isel.daw.pokerDice.http.model.Problem
import pt.isel.daw.pokerDice.services.GameCreationError
import pt.isel.daw.pokerDice.services.GameService
import pt.isel.daw.pokerDice.services.RoundService
import pt.isel.daw.pokerDice.utils.Failure
import pt.isel.daw.pokerDice.utils.Success

@RestController
class RoundController(
    private val roundService: RoundService,
) {
    @PostMapping(GameUris.Games.ROUND)
    fun startRound(
        @PathVariable("gameId") gameId: Int,
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
    ): ResponseEntity<*> {
        println("startRound chamado!")

        val res = roundService.startRound(authenticatedUser.user.id, gameId = gameId)
        return when (res) {
            is Success ->
                ResponseEntity
                    .status(201)
                    .header("Location", GameUris.Games.BY_ID.replace("{gameId}", res.value.toString()))
                    .build<Unit>()
            is Failure ->
                when (res.value) {
                    is GameCreationError.LobbyNotFound -> Problem.response(404, Problem.gameNotFound)
                    GameCreationError.GameAlreadyRunning -> Problem.response(409, Problem.gameAlreadyRunning)
                    GameCreationError.NotEnoughPlayers -> Problem.response(400, Problem.notEnoughPlayers)
                }
        }
    }
}
