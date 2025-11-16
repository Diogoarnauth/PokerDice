package pt.isel.daw.pokerDice.http.model

import org.springframework.http.ResponseEntity
import java.net.URI

class Problem(
    typeUri: URI,
    val message: String,
) {
    val type = typeUri.toASCIIString()

    companion object {
        const val MEDIA_TYPE = "application/problem+json"
        private const val BASE_URL = "https://github.com/isel-leic-daw/2025-daw-leic51d-2025-leic51d-01/tree/testes/Docs/Problems"

        fun response(
            status: Int,
            problem: Problem,
        ) = ResponseEntity
            .status(status)
            .header("Content-Type", MEDIA_TYPE)
            .body<Any>(problem)

        // User Related
        val userAlreadyInvited = Problem(URI("${BASE_URL}userAlreadyInvited"), "User already invited")
        val dontHavePermission = Problem(URI("${BASE_URL}permissionDenied"), "permission denied")
        val userNotFound = Problem(URI("${BASE_URL}userNotFound"), "user not found") // DONE
        val userAlreadyExists = Problem(URI("${BASE_URL}userAlreadyExists"), "user already exists") // DONE
        val userOrPasswordAreInvalid = Problem(URI("${BASE_URL}userOrPasswordAreInvalid"), "user or password are invalid") // DONE
        val userNotAuthorized = Problem(URI("${BASE_URL}userNotAuthorized"), "user not authorized") // DONE
        val badDeposit = Problem(URI("${BASE_URL}badDeposit"), "bad deposit") // DONE
        val AdminAlreadyExists = Problem(URI("${BASE_URL}AdminAlreadyExists"), "Admin Already Exists") // DONE
        val InvalidData = Problem(URI("${BASE_URL}InvalidData"), "Invalid Data") // DONE

        // Invitation Related
        val invitationDontExist = Problem(URI("${BASE_URL}inviteDontExist"), "invite Dont Exist") // DONE
        val inviteCreationError = Problem(URI("${BASE_URL}inviteCreationError"), "invite Creation Error") // DONE
        val invitationExpired = Problem(URI("${BASE_URL}inviteExpired"), "invite Expired") // DONE
        val invitationUsed = Problem(URI("${BASE_URL}inviteUsed"), "inviteUsed") // DONE

        // Lobby Related
        val invalidLobbySettings = Problem(URI("${BASE_URL}invalidLobbySettings"), "invalid Lobby Settings") // DONE
        val lobbyAlreadyExists = Problem(URI("${BASE_URL}lobbyAlreadyExists"), "lobby Already Exists") // DONE
        val HostAlreadyHasAnOpenLobby = Problem(URI("${BASE_URL}HostAlreadyHasAnOpenLobby"), "Host Already Has An Open Lobby") // DONE
        val HostAlreadyOnAnotherLobby = Problem(URI("${BASE_URL}HostAlreadyOnAnotherLobby"), "Host Already On Another Lobby") // DONE
        val NotEnoughCredit = Problem(URI("${BASE_URL}NotEnoughCredit"), "Not Enough Credit") // DONE
        val lobbyNotFound = Problem(URI("${BASE_URL}lobbyNotFound"), "lobby Not Found") // DONE
        val alreadyInLobby = Problem(URI("${BASE_URL}AlreadyInLobby"), "Already In Lobby") // DONE
        val lobbyFull = Problem(URI("${BASE_URL}LobbyFull"), "Lobby Full") // DONE
        val notInLobby = Problem(URI("${BASE_URL}notInLobby"), "not In Lobby") // DONE
        val onlyHostCanCloseLobby = Problem(URI("${BASE_URL}onlyHostCanCloseLobby"), "only Host Can Close Lobby")
        val LobbyAlreadyRunning = Problem(URI("${BASE_URL}LobbyAlreadyRunning"), "Lobby Already Running") // DONE
        val TurnTimeInvalid = Problem(URI("${BASE_URL}TurnTimeInvalid"), "Turn Time Invalid")

        // Game generic
        val gameNotFound = Problem(URI("${BASE_URL}gameNotFound"), "game Not Found") // DONE
        val notFirstRoll = Problem(URI("${BASE_URL}notFirstRoll"), "not First Roll") // DONE
        val TurnAlreadyFinished = Problem(URI("${BASE_URL}TurnAlreadyFinished"), "Turn Already Finished") // DONE
        val IsNotYouTurn = Problem(URI("${BASE_URL}IsNotYourTurn"), "Is Not Your Turn") // DONE
        val gameAlreadyClosed = Problem(URI("${BASE_URL}gameAlreadyClosed"), "game Already Closed") // DONE

        // val LobbyNotFound = Problem(URI("${BASE_URL}LobbyNotFound"))
        val YouAreNotHost = Problem(URI("${BASE_URL}YouAreNotHost"), "You Are Not Host") // DONE

        // Generic
        val anotherError = Problem(URI("${BASE_URL}anotherError"), "another Error") // DONE
        val insecurePassword = Problem(URI("${BASE_URL}insecurePassword"), "insecure password") // DONE
        val internalServerError = Problem(URI("${BASE_URL}InternalServerError"), "Internal Server Error")
        val invalidRequestContent = Problem(URI("${BASE_URL}invalidContent"), "invalid content") // DONE

        val gameAlreadyRunning = Problem(URI("${BASE_URL}gameAlreadyRunning"), "game Already Running") // DONE
        val notEnoughPlayers = Problem(URI("${BASE_URL}notEnoughPlayers"), "not Enough Players") // DONE
        val NotTheHost = Problem(URI("${BASE_URL}NotTheHost"), "Not The Host") // DONE

        // Round Related
        val noActiveRound = Problem(URI("${BASE_URL}noActiveRound"), "no Active Round") // DONE
        val noActiveTurn = Problem(URI("${BASE_URL}noActiveTurn"), "no Active Turn") // DONE

        // token related
        val tokenInvalid = Problem(URI("${BASE_URL}tokenInvalid"), "token Invalid") // DONE

        // val userNotInvited = Problem(URI("${BASE_URL}user-not-invited"))
        // val userNotInLobby = Problem(URI("${BASE_URL}user-not-in-channel"))
        // val noUpdateProvided = Problem(URI("${BASE_URL}no-update-provided"))
    }
}
