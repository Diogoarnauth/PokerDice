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

        val playerAlreadyInvited = Problem(URI("${BASE_URL}player-already-invited"))
        val dontHavePermission = Problem(URI("${BASE_URL}permission-denied"))
        val playerNotFound = Problem(URI("${BASE_URL}player-not-found"))
        val playerAlreadyExists = Problem(URI("${BASE_URL}player-already-exists"))
        val playerOrPasswordAreInvalid = Problem(URI("${BASE_URL}player-or-password-are-invalid"))
        val invitationDontExist = Problem(URI("${BASE_URL}invite-dont-exist"))
        val inviteCreationError = Problem(URI("${BASE_URL}invite-creation-error"))
        val invitationExpired = Problem(URI("${BASE_URL}invite-expired"))
        val invitationUsed = Problem(URI("${BASE_URL}invite-used"))
        val insecurePassword = Problem(URI("${BASE_URL}insecure-password"))
        val invalidRequestContent = Problem(URI("${BASE_URL}invalid-content"))
        //val channelAlreadyExists = Problem(URI("${BASE_URL}channel-exist"))
        //val noChannelsFound = Problem(URI("${BASE_URL}channel-not-found"))
        val playerNotAuthorized = Problem(URI("${BASE_URL}player-not-authorized"))
        //val playerNotInvited = Problem(URI("${BASE_URL}player-not-invited"))
        //val inviteExpired = Problem(URI("${BASE_URL}invite-expired"))
        //val invalidMessage = Problem(URI("${BASE_URL}invalid-message"))
        //val inviteNotFound = Problem(URI("${BASE_URL}invite-not-found"))
        //val playerNotInChannel = Problem(URI("${BASE_URL}player-not-in-channel"))
        //val playerAlreadyInThisChannel = Problem(URI("${BASE_URL}player-already-in-this-channel"))
        //val noUpdateProvided = Problem(URI("${BASE_URL}no-update-provided"))
    }
}