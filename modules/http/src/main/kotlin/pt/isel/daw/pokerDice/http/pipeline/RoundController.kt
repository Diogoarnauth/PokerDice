import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.daw.pokerDice.domain.users.AuthenticatedUser
import pt.isel.daw.pokerDice.http.GameUris
import pt.isel.daw.pokerDice.http.RoundUris
import pt.isel.daw.pokerDice.http.model.Problem
import pt.isel.daw.pokerDice.services.RoundCreationError
import pt.isel.daw.pokerDice.services.RoundService
import pt.isel.daw.pokerDice.utils.Failure
import pt.isel.daw.pokerDice.utils.Success

@RestController
class RoundController(
    private val roundService: RoundService,
) {
    @PostMapping(RoundUris.Rounds.START)
    fun startRound(
        @PathVariable("gameId") gameId: Int,
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
    ): ResponseEntity<*> {
        println("startRound chamado!")

        val res = roundService.startRound(authenticatedUser.user.id, gameId)

        return when (res) {
            is Success ->
                ResponseEntity
                    .status(201)
                    .header("Location", GameUris.Games.BY_ID.replace("{gameId}", res.value.toString()))
                    .build<Unit>()

            is Failure ->
                when (val error = res.value) {
                    is RoundCreationError.GameNotFound ->
                        Problem.response(404, Problem.gameNotFound)

                    is RoundCreationError.LobbyNotFound ->
                        Problem.response(404, Problem.lobbyNotFound)

                    RoundCreationError.NotEnoughPlayers ->
                        Problem.response(400, Problem.notEnoughPlayers)

                    RoundCreationError.InvalidRoundData ->
                        Problem.response(400, Problem.invalidRequestContent)
                }
        }
    }
}
