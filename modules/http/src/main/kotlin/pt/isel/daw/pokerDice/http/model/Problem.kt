package pt.isel.daw.pokerDice.http.model

import org.springframework.http.ResponseEntity
import java.net.URI

class Problem(
    typeUri: URI,
) {
    val type = typeUri.toASCIIString()

    companion object {
        const val MEDIA_TYPE = "application/problem+json"
        private const val BASE_URL = "TODO()"

        fun response(
            status: Int,
            problem: Problem,
        ) = ResponseEntity
            .status(status)
            .header("Content-Type", MEDIA_TYPE)
            .body<Any>(problem)


        //Player Related
        val playerAlreadyInvited = Problem(URI("${BASE_URL}player-already-invited"))
        val dontHavePermission = Problem(URI("${BASE_URL}permission-denied"))
        val playerNotFound = Problem(URI("${BASE_URL}player-not-found"))
        val playerAlreadyExists = Problem(URI("${BASE_URL}player-already-exists"))
        val playerOrPasswordAreInvalid = Problem(URI("${BASE_URL}player-or-password-are-invalid"))
        val playerNotAuthorized = Problem(URI("${BASE_URL}player-not-authorized"))


        //Invitation Related
        val invitationDontExist = Problem(URI("${BASE_URL}invite-dont-exist"))
        val inviteCreationError = Problem(URI("${BASE_URL}invite-creation-error"))
        val invitationExpired = Problem(URI("${BASE_URL}invite-expired"))
        val invitationUsed = Problem(URI("${BASE_URL}invite-used"))

        //Lobby Related
        val invalidLobbySettings = Problem(URI("${BASE_URL}invalid-Lobby-Settings"))
        val lobbyAlreadyExists = Problem(URI("${BASE_URL}lobbyAlreadyExists"))
        val HostAlreadyHasAnOpenLobby = Problem(URI("${BASE_URL}HostAlreadyHasAnOpenLobby"))
        val HostAlreadyOnAnotherLobby = Problem(URI("${BASE_URL}HostAlreadyOnAnotherLobby"))
        val NotEnoughCredit = Problem(URI("${BASE_URL}NotEnoughCredit"))
        val lobbyNotFound = Problem(URI("${BASE_URL}lobbyNotFound"))
        val alreadyInLobby = Problem(URI("${BASE_URL}AlreadyInLobby"))
        val lobbyFull = Problem(URI("${BASE_URL}LobbyFull"))


        //Generic
        val insecurePassword = Problem(URI("${BASE_URL}insecure-password"))
        val internalServerError = Problem(URI("${BASE_URL}InternalServerError"))
        val invalidRequestContent = Problem(URI("${BASE_URL}invalid-content"))





        //val playerNotInvited = Problem(URI("${BASE_URL}player-not-invited"))
        //val playerNotInLobby = Problem(URI("${BASE_URL}player-not-in-channel"))
        //val noUpdateProvided = Problem(URI("${BASE_URL}no-update-provided"))
    }
}