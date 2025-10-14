package pt.isel.daw.pokerDice.http.pipeline

import org.springframework.stereotype.Component
import pt.isel.daw.pokerDice.domain.users.AuthenticatedUser
import pt.isel.daw.pokerDice.services.UsersService

@Component
class RequestTokenProcessor(
    val playerService: UsersService,
) {
    fun processAuthorizationHeaderValue(authorizationValue: String?): AuthenticatedUser? {
        if (authorizationValue == null) {
            return null
        }
        val parts = authorizationValue.trim().split(" ")
        if (parts.size != 2) {
            return null
        }
        if (parts[0].lowercase() != SCHEME) {
            return null
        }
        return playerService.getUserByToken(parts[1])?.let {
            AuthenticatedUser(
                it,
                parts[1],
            )
        }
    }

    companion object {
        const val SCHEME = "bearer"
    }
}
