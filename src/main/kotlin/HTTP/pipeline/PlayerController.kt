package org.example.HTTP.pipeline

import PlayersService
import com.sun.net.httpserver.Authenticator
import org.example.HTTP.model.CreatePlayerOutputModel
import org.example.HTTP.model.GetByIdOutputModel
import org.example.HTTP.model.PlayerCreateInputModel
import org.example.HTTP.model.Problem
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController


@RestController
class PlayerController(
    private val playerService: PlayersService,
) {
    /*@PostMapping(Uris.Users.CREATE)
    fun create(
        @RequestBody input: UserCreateInputModel,
    ): ResponseEntity<*> {
        val res = userService.createUser(input.username, input.password)
        return when (res) {
            is Success ->
                ResponseEntity.status(201)
                    .header(
                        "Location",
                        Uris.Users.byId(res.value).toASCIIString(),
                    ).build<Unit>()

            is Failure ->
                when (res.value) {
                    UserCreationError.InsecurePassword -> Problem.response(400, Problem.insecurePassword)
                    UserCreationError.UserAlreadyExists -> Problem.response(400, Problem.userAlreadyExists)
                }
        }
    }

    @PostMapping(Uris.Users.TOKEN)
    fun token(
        @RequestBody input: UserCreateTokenInputModel,
    ): ResponseEntity<*> {
        val res = userService.createToken(input.username, input.password)
        return when (res) {
            is Success ->
                ResponseEntity.status(200)
                    .body(UserTokenCreateOutputModel(res.value.tokenValue))

            is Failure ->
                when (res.value) {
                    TokenCreationError.UserOrPasswordAreInvalid ->
                        Problem.response(400, Problem.userOrPasswordAreInvalid)
                }
        }
    }

    @PostMapping(Uris.Users.LOGOUT)
    fun logout(user: AuthenticatedUser) {
        userService.revokeToken(user.token)
    }
*/

    @DeleteMapping(PlayerUris.Players.GET_BY_ID) // mesmo path de GET, mas método DELETE
    fun deletePlayer(
        @PathVariable id: String,
        player: AuthenticatedPlayer,             // já vem do ArgumentResolver
    ): ResponseEntity<*> {

        // 1) validar path param
        val playerId = id.toIntOrNull()
            ?: return Problem.response(400, Problem.invalidRequestContent)

        // 2) (opcional mas recomendado) só pode apagar o próprio
        if (player.id != playerId) {
            return ResponseEntity.status(403).build<Unit>() // Forbidden
        }

        // 3) pedir ao serviço
        return when (val res = playersService.deleteById(playerId)) {
            is Authenticator.Success -> ResponseEntity.noContent().build<Unit>()       // 204
            is Authenticator.Failure -> ResponseEntity.status(404).build<Unit>()       // Not Found
            else -> ResponseEntity.status(500).build<Unit>() // ver se é isto que temos de ter
        }
    }


    @GetMapping(PlayerUris.Players.GET_BY_ID)
    fun getById(@PathVariable id: String): ResponseEntity<*> {
        // 1) Validar o path param (tem de ser número)
        val playerId = id.toIntOrNull()
            ?: return Problem.response(400, Problem.invalidRequestContent)

        // 2) Pedir ao serviço
        val res = playerService.getPlayerById(playerId)

        // 3) Mapear resultado → HTTP
        return when (res) {
            is Authenticator.Success -> ResponseEntity.ok(
                GetByIdOutputModel(
                    id = res.value.id,          // adapta o tipo se for Long
                    username = res.value.username,
                    name = res.value.name,
                    age = res.value.age,
                    credit = res.value.credit,
                    winCounter = res.value.WinCounter

                )
            )
            is Authenticator.Failure -> ResponseEntity.status(404).build<Unit>() // not found
            else -> ResponseEntity.status(500).build<Unit>() // ver se é isto que temos de ter
        }
    }

    @PostMapping(PlayerUris.Players.CREATE)
    fun createPlayer(
        @RequestBody input: PlayerCreateInputModel
    ): ResponseEntity<*> {
        val result = playerService.createPlayer(input.username, input.password)

        return if (result != null) {
            // 201 Created + corpo JSON com o ID
            ResponseEntity
                .status(201)
                .body(CreatePlayerOutputModel(result.id))
        } else {
            // 400 Bad Request se não conseguir criar
            Problem.response(400, Problem.playerAlreadyExists)
        }
    }

/*
    @GetMapping(Uris.Users.HOME)
    fun getUserHome(userAuthenticatedUser: AuthenticatedUser): UserHomeOutputModel {
        return UserHomeOutputModel(
            id = userAuthenticatedUser.user.id,
            username = userAuthenticatedUser.user.username,
        )
    }*/
}