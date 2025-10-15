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
import pt.isel.daw.pokerDice.services.CloseLobbyError
import pt.isel.daw.pokerDice.services.CreateLobbyError
import pt.isel.daw.pokerDice.services.JoinLobbyError
import pt.isel.daw.pokerDice.services.LeaveLobbyError
import pt.isel.daw.pokerDice.services.LobbiesService
import pt.isel.daw.pokerDice.services.LobbyGetByIdError
import pt.isel.daw.pokerDice.utils.Failure
import pt.isel.daw.pokerDice.utils.Success

@RestController
class LobbiesController(
    private val lobbies: LobbiesService,
) {
    // GET /lobbies → lista lobbies visíveis (ainda não cheios)
    @GetMapping(LobbyUris.Lobbies.LIST)
    fun list(): ResponseEntity<*> {
        // val list = lobbies.getVisibleLobbies() TODO("NEVER USED")
        return ResponseEntity.ok(lobbies)
    }

    // POST /lobbies → cria lobby (auth requerido)
    @PostMapping(LobbyUris.Lobbies.CREATE)
    fun create(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
        @RequestBody body: LobbyCreateInputModel,
    ): ResponseEntity<*> {
        val res =
            lobbies.createLobby(
                authenticatedUser.user.id,
                body.name,
                body.description,
                body.isPrivate,
                body.passwordValidationInfo,
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
                    CreateLobbyError.HostAlreadyHasAnOpenLobby -> Problem.response(400, Problem.HostAlreadyHasAnOpenLobby)
                    CreateLobbyError.HostAlreadyOnAnotherLobby -> Problem.response(400, Problem.HostAlreadyOnAnotherLobby)
                    CreateLobbyError.NotEnoughCredit -> Problem.response(400, Problem.NotEnoughCredit)
                    CreateLobbyError.InsecurePassword -> Problem.response(400, Problem.insecurePassword)

                    else -> Problem.response(500, Problem.internalServerError)
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

        val res = lobbies.getLobbyById(lobbyId)

        return when (res) {
            is Success -> {
                val lobby = res.value
                ResponseEntity.ok(
                    LobbyGetByIdOutputModel(
                        id = lobby.id,
                        name = lobby.name,
                        description = lobby.description,
                        hostId = lobby.hostId,
                        isPrivate = lobby.isPrivate,
                        minUsers = lobby.minUsers,
                        maxUsers = lobby.maxUsers,
                        rounds = lobby.rounds,
                        minCreditToParticipate = lobby.minCreditToParticipate,
                    ),
                )
            }

            is Failure ->
                when (res.value) {
                    LobbyGetByIdError.LobbyNotFound ->
                        Problem.response(404, Problem.lobbyNotFound)

                    else -> {
                        TODO()
                    }
                }

            else -> {
                TODO()
            }
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

        val res = lobbies.joinLobby(lobbyId, authenticatedUser.user.id)

        return when (res) {
            is Success -> ResponseEntity.noContent().build<Unit>()

            is Failure ->
                when (res.value) {
                    JoinLobbyError.LobbyNotFound -> Problem.response(404, Problem.lobbyNotFound)
                    JoinLobbyError.LobbyFull -> Problem.response(409, Problem.lobbyFull)
                    JoinLobbyError.AlreadyInLobby -> Problem.response(409, Problem.alreadyInLobby)
                    JoinLobbyError.InsufficientCredits -> Problem.response(403, Problem.NotEnoughCredit)
                }

            else -> {
                TODO()
            }
        }
    }

    // DELETE /lobbies/{id}/users/me → sair do lobby
    @DeleteMapping(LobbyUris.Lobbies.LEAVE_ME)
    fun leave(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
        @PathVariable id: String,
    ): ResponseEntity<*> {
        val lobbyId =
            id.toIntOrNull()
                ?: return Problem.response(400, Problem.invalidRequestContent)

        val res = lobbies.leaveLobby(lobbyId, authenticatedUser.user.id)

        return when (res) {
            is Success -> ResponseEntity.noContent().build<Unit>()

            is Failure ->
                when (res.value) {
                    LeaveLobbyError.LobbyNotFound -> Problem.response(404, Problem.lobbyNotFound)
                    LeaveLobbyError.NotInLobby -> Problem.response(409, Problem.notInLobby)
                }

            else -> {
                TODO()
            }
        }
    }

    // DELETE /lobbies/{id} → o host encerra o lobby completamente
    @DeleteMapping(LobbyUris.Lobbies.CLOSE)
    fun closeLobby(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
        @PathVariable id: String,
    ): ResponseEntity<*> {
        val lobbyId =
            id.toIntOrNull()
                ?: return Problem.response(400, Problem.invalidRequestContent)

        val res = lobbies.closeLobby(lobbyId, authenticatedUser.user.id)

        return when (res) {
            is Success -> ResponseEntity.noContent().build<Unit>()
            is Failure ->
                when (res.value) {
                    CloseLobbyError.LobbyNotFound -> Problem.response(404, Problem.lobbyNotFound)
                    CloseLobbyError.NotHost -> Problem.response(403, Problem.onlyHostCanCloseLobby)
                }

            else -> {
                TODO()
            }
        }
    }
}
