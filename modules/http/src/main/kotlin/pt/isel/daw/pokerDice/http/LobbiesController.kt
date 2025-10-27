package pt.isel.daw.pokerDice.http

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pt.isel.daw.pokerDice.domain.users.AuthenticatedUser
import pt.isel.daw.pokerDice.http.model.Problem
import pt.isel.daw.pokerDice.http.model.lobbyModel.LobbyCreateInputModel
import pt.isel.daw.pokerDice.http.model.lobbyModel.LobbyGetByIdOutputModel
import pt.isel.daw.pokerDice.services.CreateLobbyError
import pt.isel.daw.pokerDice.services.JoinLobbyError
import pt.isel.daw.pokerDice.services.LeaveLobbyError
import pt.isel.daw.pokerDice.services.LobbiesService
import pt.isel.daw.pokerDice.utils.Failure
import pt.isel.daw.pokerDice.utils.Success

@RestController
class LobbiesController(
    private val lobbiesServices: LobbiesService,
) {
    // GET /lobbies → lista lobbies visíveis (ainda não cheios)
    @GetMapping(LobbyUris.Lobbies.LIST)
    fun list(): ResponseEntity<*> {
        val lista = lobbiesServices.getVisibleLobbies() // TODO ("NEVER USED")
        return ResponseEntity.ok(lista)
    }

    // POST /lobbies → cria lobby (auth requerido)
    @PostMapping(LobbyUris.Lobbies.CREATE)
    fun create(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
        @RequestBody body: LobbyCreateInputModel,
    ): ResponseEntity<*> {
        val res =
            lobbiesServices.createLobby(
                authenticatedUser.user.id,
                body.name,
                body.description,
                body.minUsers,
                body.maxUsers,
                body.rounds,
                body.minCreditToParticipate,
            )

        return when (res) {
            is Success ->
                ResponseEntity
                    .status(201)
                    .header("Location", LobbyUris.Lobbies.BY_ID.replace("{id}", res.value.toString()))
                    .build<Unit>()

            is Failure ->
                when (res.value) {
                    CreateLobbyError.InvalidSettings -> Problem.response(400, Problem.invalidLobbySettings)
                    CreateLobbyError.CouldNotCreateLobby -> Problem.response(400, Problem.lobbyAlreadyExists)
                    CreateLobbyError.HostAlreadyHasAnOpenLobby -> Problem.response(403, Problem.HostAlreadyHasAnOpenLobby)
                    CreateLobbyError.HostAlreadyOnAnotherLobby -> Problem.response(409, Problem.HostAlreadyOnAnotherLobby)
                    CreateLobbyError.NotEnoughCredit -> Problem.response(401, Problem.NotEnoughCredit)
                    CreateLobbyError.InsecurePassword -> Problem.response(400, Problem.insecurePassword)
                }
        }
    }

    // GET /lobbies/{id} → detalhes do lobby
    @GetMapping(LobbyUris.Lobbies.BY_ID)
    fun getById(
        @PathVariable id: String,
    ): ResponseEntity<*> {
        val lobbyId =
            id.toIntOrNull()
                ?: return Problem.response(400, Problem.invalidRequestContent)

        val res = lobbiesServices.getLobbyById(lobbyId)

        return when (res) {
            is Success -> {
                val lobby = res.value
                ResponseEntity.ok(
                    LobbyGetByIdOutputModel(
                        id = lobby.id,
                        name = lobby.name,
                        description = lobby.description,
                        hostId = lobby.hostId,
                        minUsers = lobby.minUsers,
                        maxUsers = lobby.maxUsers,
                        rounds = lobby.rounds,
                        minCreditToParticipate = lobby.minCreditToParticipate,
                    ),
                )
            }

            is Failure -> Problem.response(404, Problem.lobbyNotFound)
        }
    }

    // POST /lobbies/{id}/users → entrar no lobby
    @PostMapping(LobbyUris.Lobbies.JOIN)
    fun join(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
        @PathVariable id: String,
    ): ResponseEntity<*> {
        val lobbyId =
            id.toIntOrNull()
                ?: return Problem.response(400, Problem.invalidRequestContent)

        val res = lobbiesServices.joinLobby(lobbyId, authenticatedUser.user.id)

        return when (res) {
            is Success -> ResponseEntity.ok(mapOf("message" to "Joined lobby $lobbyId"))

            is Failure ->
                when (res.value) {
                    JoinLobbyError.LobbyNotFound -> Problem.response(404, Problem.lobbyNotFound)
                    JoinLobbyError.LobbyFull -> Problem.response(409, Problem.lobbyFull)
                    JoinLobbyError.AlreadyInLobby -> Problem.response(409, Problem.alreadyInLobby)
                    JoinLobbyError.InsufficientCredits -> Problem.response(403, Problem.NotEnoughCredit)
                    JoinLobbyError.LobbyAlreadyRunning -> Problem.response(409, Problem.LobbyAlreadyRunning)
                }
        }
    }

    // DELETE /lobbies/{id}/leave → sair do lobby
    @DeleteMapping(LobbyUris.Lobbies.LEAVE_ME)
    fun leave(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
        @PathVariable id: String,
    ): ResponseEntity<*> {
        val lobbyId =
            id.toIntOrNull()
                ?: return Problem.response(400, Problem.invalidRequestContent)

        val res = lobbiesServices.leaveLobby(lobbyId, authenticatedUser.user.id)

        // TODO("404 NOT FOUND CASO EU SEJA O HOST DESSE LOBBY, MAS POSSO DAR CLOSE")

        return when (res) {
            is Success -> ResponseEntity.ok(mapOf("message" to "Left lobby $lobbyId"))

            is Failure ->
                when (res.value) {
                    LeaveLobbyError.LobbyNotFound -> Problem.response(404, Problem.lobbyNotFound)
                    LeaveLobbyError.NotInLobby -> Problem.response(409, Problem.notInLobby)
                }
        }
    }

/*
    // DELETE /lobbies/{id} → o host encerra o lobby completamente
    @DeleteMapping(LobbyUris.Lobbies.CLOSE)
    fun closeLobby(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
        @PathVariable id: String,
    ): ResponseEntity<*> {
        val lobbyId =
            id.toIntOrNull()
                ?: return Problem.response(400, Problem.invalidRequestContent)

        val res = lobbiesServices.closeLobby(lobbyId, authenticatedUser.user.id)

        return when (res) {
            is Success -> ResponseEntity.ok(mapOf("message" to "Lobby $lobbyId closed"))
            is Failure ->
                when (res.value) {
                    CloseLobbyError.LobbyNotFound -> Problem.response(404, Problem.lobbyNotFound)
                    CloseLobbyError.NotHost -> Problem.response(403, Problem.onlyHostCanCloseLobby)
                }

            else -> {
                TODO()
            }
        }
    }*/
}
