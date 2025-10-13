package pt.isel.daw.pokerDice.http

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pt.isel.daw.pokerDice.domain.players.AuthenticatedPlayer
import pt.isel.daw.pokerDice.domain.players.Player
import pt.isel.daw.pokerDice.http.model.*
import pt.isel.daw.pokerDice.http.model.InviteModel.InviteAppOutputModel
import pt.isel.daw.pokerDice.http.model.PlayerModel.*
import pt.isel.daw.pokerDice.services.*
import pt.isel.daw.pokerDice.utils.*

@RestController
class PlayerController(
    private val playerService: PlayersService,
) {


    @PostMapping("/bootstrap")
    fun bootstrapAdmin(@RequestBody input: BootstrapRegisterInputModel): ResponseEntity<*> {
        if (playerService.hasAnyPlayer()) {
            return ResponseEntity.status(403).body("Bootstrap already done")
        }
        val id = playerService.bootstrapFirstPlayer(input.username, input.name, input.age, input.password)
        return ResponseEntity.ok(id)
    }


    @PostMapping(PlayerUris.Players.CREATE)
    fun create(
        @RequestBody input: PlayerCreateInputModel,
    ): ResponseEntity<*> {
        val res = playerService.createPlayer(input.username,input.name, input.age, input.password, input.inviteCode)
        return when (res) {
            is Success ->
                ResponseEntity.status(201)
                    .header(
                        "Location",
                        PlayerUris.Players.byId(res.value).toASCIIString(),
                    ).build<Unit>()

            is Failure ->
                when (res.value) {
                    PlayerRegisterError.InsecurePassword -> Problem.response(400, Problem.insecurePassword)
                    PlayerRegisterError.PlayerAlreadyExists -> Problem.response(400, Problem.playerAlreadyExists)

                    PlayerRegisterError.InvitationDontExist -> Problem.response(400, Problem.invitationDontExist)
                    PlayerRegisterError.InvitationUsed -> Problem.response(400, Problem.invitationUsed)
                    PlayerRegisterError.InvitationExpired -> Problem.response(400, Problem.invitationExpired)

                    else -> {TODO()} //dúvidas
                }

            else -> {TODO()}//dúvidas
        }
    }

    @PostMapping(PlayerUris.Players.TOKEN)
    fun token(
        @RequestBody input: PlayerCreateTokenInputModel,
    ): ResponseEntity<*> {
        val res = playerService.createToken(input.username, input.password)
        return when (res) {
            is Success ->
                ResponseEntity.status(200)
                    .body(PlayerTokenCreateOutputModel(res.value.tokenValue))

            is Failure ->
                when (res.value) {
                    TokenCreationError.PlayerOrPasswordAreInvalid ->
                        Problem.response(400, Problem.playerOrPasswordAreInvalid)

                    else -> {TODO()}
                }

            else -> {TODO()}
        }
    }


    @GetMapping(PlayerUris.Players.GET_BY_ID)
    fun getById(
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val res = playerService.getById(id)

        return when (res) {
            is Success -> {
                val player: Player = res.value
                ResponseEntity.status(200)
                    .body(
                        PlayerGetByIdOutputModel(
                            username = player.username,
                            token = player.token,
                            name = player.name,
                        )
                    )
            }

            is Failure -> when (res.value) {
                PlayerGetByIdError.PlayerNotFound ->
                    Problem.response(404, Problem.playerNotFound)
            }
        }
    }

    @PostMapping(PlayerUris.Players.INVITE)
    fun appInvite(AuthenticatedPlayer: AuthenticatedPlayer): ResponseEntity<*> {
        val res = playerService.createAppInvite(AuthenticatedPlayer.player.id)
        return when (res) {
            is Success ->
                ResponseEntity
                    .status(201)
                    .body(InviteAppOutputModel(res.value))

            is Failure ->
                Problem.response(400, Problem.inviteCreationError)
        }
    }

}