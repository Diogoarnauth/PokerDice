package org.example.HTTP

import Failure
import PlayersService
import Success
import com.sun.net.httpserver.Authenticator
import org.example.HTTP.model.CreatePlayerOutputModel
import org.example.HTTP.model.GetByIdOutputModel
import org.example.HTTP.model.PlayerCreateInputModel
import org.example.HTTP.model.Problem
import org.example.HTTP.pipeline.PlayerUris
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

    @PostMapping(PlayerUris.Players.CREATE)
    fun create(
        @RequestBody input: PlayerCreateInputModel,
    ): ResponseEntity<*> {
        val res = playerService.createPlayer(input.username,input.name, input.age, input.password)
        return when (res) {
            is Success ->
                ResponseEntity.status(201)
                    .header(
                        "Location",
                        PlayerUris.Players.byId(res.value).toASCIIString(),
                    ).build<Unit>()

            is Failure ->
                when (res.value) {
                    PlayerCreationError.InsecurePassword -> Problem.response(400, Problem.insecurePassword)
                    PlayerCreationError.PlayerAlreadyExists -> Problem.response(400, Problem.playerAlreadyExists)
                    else -> {TODO()} //dúvidas
                }

            else -> {TODO()}//dúvidas
        }
    }


}