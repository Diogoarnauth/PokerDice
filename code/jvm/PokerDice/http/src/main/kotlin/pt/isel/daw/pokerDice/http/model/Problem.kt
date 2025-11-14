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
        val userNotFound = Problem(URI("${BASE_URL}user-not-found")) // DONE
        val userAlreadyExists = Problem(URI("${BASE_URL}user-already-exists")) // DONE
        val userOrPasswordAreInvalid = Problem(URI("${BASE_URL}user-or-password-are-invalid")) // DONE
        val userNotAuthorized = Problem(URI("${BASE_URL}user-not-authorized")) // DONE
        val badDeposit = Problem(URI("${BASE_URL}bad-deposit")) // DONE
        val AdminAlreadyExists = Problem(URI("${BASE_URL}AdminAlreadyExists")) // DONE
        val InvalidData = Problem(URI("${BASE_URL}InvalidData")) // DONE

        // Invitation Related
        val invitationDontExist = Problem(URI("${BASE_URL}invite-dont-exist")) // DONE
        val inviteCreationError = Problem(URI("${BASE_URL}invite-creation-error")) // DONE
        val invitationExpired = Problem(URI("${BASE_URL}invite-expired")) // DONE
        val invitationUsed = Problem(URI("${BASE_URL}invite-used")) // DONE

        // Lobby Related
        val invalidLobbySettings = Problem(URI("${BASE_URL}invalid-Lobby-Settings")) // DONE
        val lobbyAlreadyExists = Problem(URI("${BASE_URL}lobbyAlreadyExists")) // DONE
        val HostAlreadyHasAnOpenLobby = Problem(URI("${BASE_URL}HostAlreadyHasAnOpenLobby")) // DONE
        val HostAlreadyOnAnotherLobby = Problem(URI("${BASE_URL}HostAlreadyOnAnotherLobby")) // DONE
        val NotEnoughCredit = Problem(URI("${BASE_URL}NotEnoughCredit")) // DONE
        val lobbyNotFound = Problem(URI("${BASE_URL}lobbyNotFound")) // DONE
        val alreadyInLobby = Problem(URI("${BASE_URL}AlreadyInLobby")) // DONE
        val lobbyFull = Problem(URI("${BASE_URL}LobbyFull")) // DONE
        val notInLobby = Problem(URI("${BASE_URL}notInLobby")) // DONE
        val onlyHostCanCloseLobby = Problem(URI("${BASE_URL}onlyHostCanCloseLobby"))
        val LobbyAlreadyRunning = Problem(URI("${BASE_URL}LobbyAlreadyRunning")) // DONE

        // Game generic
        val gameNotFound = Problem(URI("${BASE_URL}gameNotFound")) // DONE
        val notFirstRoll = Problem(URI("${BASE_URL}notFirstRoll")) // DONE
        val TurnAlreadyFinished = Problem(URI("${BASE_URL}TurnAlreadyFinished")) // DONE
        val IsNotYouTurn = Problem(URI("${BASE_URL}IsNotYouTurn")) // DONE
        val gameAlreadyClosed = Problem(URI("${BASE_URL}gameAlreadyClosed")) // DONE

        // val LobbyNotFound = Problem(URI("${BASE_URL}LobbyNotFound"))
        val YouAreNotHost = Problem(URI("${BASE_URL}YouAreNotHost")) // DONE

        // Generic
        val anotherError = Problem(URI("${BASE_URL}anotherError")) // DONE
        val insecurePassword = Problem(URI("${BASE_URL}insecure-password")) // DONE
        val internalServerError = Problem(URI("${BASE_URL}InternalServerError"))
        val invalidRequestContent = Problem(URI("${BASE_URL}invalid-content")) // DONE

        val gameAlreadyRunning = Problem(URI("${BASE_URL}game-already-running")) // DONE
        val notEnoughPlayers = Problem(URI("${BASE_URL}not-enough-players")) // DONE
        val NotTheHost = Problem(URI("${BASE_URL}NotTheHost")) // DONE

        // Round Related
        val noActiveRound = Problem(URI("${BASE_URL}noActiveRound")) // DONE
        val noActiveTurn = Problem(URI("${BASE_URL}noActiveTurn")) // DONE

        // token related
        val tokenInvalid = Problem(URI("${BASE_URL}tokenInvalid")) // DONE

        // val userNotInvited = Problem(URI("${BASE_URL}user-not-invited"))
        // val userNotInLobby = Problem(URI("${BASE_URL}user-not-in-channel"))
        // val noUpdateProvided = Problem(URI("${BASE_URL}no-update-provided"))
    }
}
