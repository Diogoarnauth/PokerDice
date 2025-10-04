package org.example.HTTP.pipeline

import org.example.HTTP.model.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.example.HTTP.model.Problem // ajusta import conforme o teu pacote
import org.example.HTTP.pipeline.LobbyUris as Uris


@RestController
class LobbiesController(
    private val lobbies: LobbiesService
) {

    // GET /lobbies → lista lobbies visíveis (ainda não cheios)
    @GetMapping(Uris.Lobbies.LIST)
    fun list(): ResponseEntity<*> =
        ResponseEntity.ok(lobbies.listOpen())

    // POST /lobbies → cria lobby (auth requerido)
    @PostMapping(Uris.Lobbies.CREATE)
    fun create(
        user: AuthenticatedUser,
        @RequestBody body: LobbyCreateInputModel
    ): ResponseEntity<*> =
        when (val res = lobbies.create(user.id, body)) {
            is CreateLobbyResult.Ok ->
                ResponseEntity.status(201)
                    .header("Location", "${Uris.Lobbies.BY_ID.replace("{id}", res.lobbyId.toString())}")
                    .build<Any>()
            is CreateLobbyResult.InvalidSettings ->
                Problem.response(400, Problem.invalidLobbySettings)
        }

    // GET /lobbies/{id} → detalhes do lobby
    @GetMapping(Uris.Lobbies.BY_ID)
    fun getById(@PathVariable id: String): ResponseEntity<*> {
        val lobbyId = id.toIntOrNull()
            ?: return Problem.response(400, Problem.invalidRequestContent)

        return when (val res = lobbies.getById(lobbyId)) {
            is GetLobbyResult.Ok -> ResponseEntity.ok(
                CreateLobbyOutputModel(
                    id = res.id,
                    name = res.name,
                    description = res.description,
                    owner = res.hostId,
                   // players = res.players,
                    minPlayers = res.minPlayers,
                    maxPlayers = res.maxPlayers,
                    nRounds = res.rounds
                )
            )
            is GetLobbyResult.NotFound -> Problem.response(404, Problem.lobbyNotFound)
        }
    }

    // POST /lobbies/{id}/players → entrar no lobby
    @PostMapping(Uris.Lobbies.JOIN)
    fun join(
        user: AuthenticatedUser,
        @PathVariable id: String
    ): ResponseEntity<*> {
        val lobbyId = id.toIntOrNull()
            ?: return Problem.response(400, Problem.invalidRequestContent)

        return when (val res = lobbies.join(lobbyId, user.id)) {
            is JoinLobbyResult.Ok -> ResponseEntity.noContent().build<Unit>()
            is JoinLobbyResult.NotFound -> Problem.response(404, Problem.lobbyNotFound)
            is JoinLobbyResult.Full -> Problem.response(409, Problem.lobbyFull)
            is JoinLobbyResult.AlreadyIn -> Problem.response(409, Problem.alreadyInLobby)
        }
    }

    // DELETE /lobbies/{id}/players/me → sair do lobby
    @DeleteMapping(Uris.Lobbies.LEAVE_ME)
    fun leave(
        user: AuthenticatedUser,
        @PathVariable id: String
    ): ResponseEntity<*> {
        val lobbyId = id.toIntOrNull()
            ?: return Problem.response(400, Problem.invalidRequestContent)

        return when (val res = lobbies.leave(lobbyId, user.id)) {
            is LeaveLobbyResult.Ok -> ResponseEntity.noContent().build<Unit>()
            is LeaveLobbyResult.NotFound -> Problem.response(404, Problem.lobbyNotFound)
            is LeaveLobbyResult.NotInLobby -> Problem.response(409, Problem.notInLobby)
        }
    }
}
