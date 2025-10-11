package pt.isel.daw.pokerDice.http

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pt.isel.daw.pokerDice.domain.players.AuthenticatedPlayer
import pt.isel.daw.pokerDice.domain.players.Player
import pt.isel.daw.pokerDice.http.model.*
import pt.isel.daw.pokerDice.services.*
import pt.isel.daw.pokerDice.utils.*

@RestController
class LobbiesController(
    private val lobbies: LobbiesService
) {

    // GET /lobbies → lista lobbies visíveis (ainda não cheios)
    @GetMapping(LobbyUris.Lobbies.LIST)
    fun list(): ResponseEntity<*> {
        val list = LobbiesService.listAllLobbies()
        return ResponseEntity.ok(lobbies)
    }


    /*  // POST /lobbies → cria lobby (auth requerido)
      @PostMapping(LobbyUris.Lobbies.CREATE)
      fun create(
          user: AuthenticatedPlayer,
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
      @GetMapping(LobbyUris.Lobbies.BY_ID)
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
      @PostMapping(LobbyUris.Lobbies.JOIN)
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
      @DeleteMapping(LobbyUris.Lobbies.LEAVE_ME)
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
      }*/
}
