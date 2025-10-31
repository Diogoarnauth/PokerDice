package pt.isel.daw.pokerDice.http.model

import org.springframework.http.ResponseEntity
import java.net.URI

class Problem(
    typeUri: URI,
) {
    val type = typeUri.toASCIIString()

    companion object {
        const val MEDIA_TYPE = "application/problem+json"
        private const val BASE_URL = "http://localhost:8080/api/"

        fun response(
            status: Int,
            problem: Problem,
        ) = ResponseEntity
            .status(status)
            .header("Content-Type", MEDIA_TYPE)
            .body<Any>(problem)

        // User Related
        val userAlreadyInvited = Problem(URI("${BASE_URL}user-already-invited"))
        val todo = Problem(URI("${BASE_URL})TODO")) // TODO(ACERTAR OS ERROS)
        val dontHavePermission = Problem(URI("${BASE_URL}permission-denied"))
        val userNotFound = Problem(URI("${BASE_URL}user-not-found"))
        val userAlreadyExists = Problem(URI("${BASE_URL}user-already-exists"))
        val userOrPasswordAreInvalid = Problem(URI("${BASE_URL}user-or-password-are-invalid"))
        val userNotAuthorized = Problem(URI("${BASE_URL}user-not-authorized"))
        val badDeposit = Problem(URI("${BASE_URL}bad-deposit"))
        val AdminAlreadyExists = Problem(URI("${BASE_URL}AdminAlreadyExists"))
        val InvalidData = Problem(URI("${BASE_URL}InvalidData"))

        // Invitation Related
        val invitationDontExist = Problem(URI("${BASE_URL}invite-dont-exist"))
        val inviteCreationError = Problem(URI("${BASE_URL}invite-creation-error"))
        val invitationExpired = Problem(URI("${BASE_URL}invite-expired"))
        val invitationUsed = Problem(URI("${BASE_URL}invite-used"))

        // Lobby Related
        val invalidLobbySettings = Problem(URI("${BASE_URL}invalid-Lobby-Settings"))
        val lobbyAlreadyExists = Problem(URI("${BASE_URL}lobbyAlreadyExists"))
        val HostAlreadyHasAnOpenLobby = Problem(URI("${BASE_URL}HostAlreadyHasAnOpenLobby"))
        val HostAlreadyOnAnotherLobby = Problem(URI("${BASE_URL}HostAlreadyOnAnotherLobby"))
        val NotEnoughCredit = Problem(URI("${BASE_URL}NotEnoughCredit"))
        val lobbyNotFound = Problem(URI("${BASE_URL}lobbyNotFound"))
        val alreadyInLobby = Problem(URI("${BASE_URL}AlreadyInLobby"))
        val lobbyFull = Problem(URI("${BASE_URL}LobbyFull"))
        val notInLobby = Problem(URI("${BASE_URL}notInLobby"))
        val onlyHostCanCloseLobby = Problem(URI("${BASE_URL}onlyHostCanCloseLobby"))
        val LobbyAlreadyRunning = Problem(URI("${BASE_URL}LobbyAlreadyRunning"))

        // Game generic
        val gameNotFound = Problem(URI("${BASE_URL}gameNotFound"))
        val notFirstRoll = Problem(URI("${BASE_URL}notFirstRoll"))
        val TurnAlreadyFinished = Problem(URI("${BASE_URL}TurnAlreadyFinished"))

        // Generic
        val anotherError = Problem(URI("${BASE_URL}anotherError"))
        val insecurePassword = Problem(URI("${BASE_URL}insecure-password"))
        val internalServerError = Problem(URI("${BASE_URL}InternalServerError"))
        val invalidRequestContent = Problem(URI("${BASE_URL}invalid-content"))

        val gameAlreadyRunning = Problem(URI("${BASE_URL}game-already-running"))
        val notEnoughPlayers = Problem(URI("${BASE_URL}not-enough-players"))
        val NotTheHost = Problem(URI("${BASE_URL}NotTheHost"))

        // Round Related
        val noActiveRound = Problem(URI("${BASE_URL}noActiveRound"))
        val noActiveTurn = Problem(URI("${BASE_URL}noActiveTurn"))

        // val userNotInvited = Problem(URI("${BASE_URL}user-not-invited"))
        // val userNotInLobby = Problem(URI("${BASE_URL}user-not-in-channel"))
        // val noUpdateProvided = Problem(URI("${BASE_URL}no-update-provided"))
    }
}
