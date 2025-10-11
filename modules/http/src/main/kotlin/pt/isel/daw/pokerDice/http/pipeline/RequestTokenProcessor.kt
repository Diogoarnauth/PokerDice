package pt.isel.daw.pokerDice.http.pipeline

import org.springframework.stereotype.Component
import pt.isel.daw.pokerDice.domain.players.AuthenticatedPlayer
import pt.isel.daw.pokerDice.services.PlayersService

@Component
class RequestTokenProcessor(
    val playerService: PlayersService,
) {
    fun processAuthorizationHeaderValue(authorizationValue: String?): AuthenticatedPlayer? {
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
        return playerService.getPlayerByToken(parts[1])?.let {
            AuthenticatedPlayer(
                it,
                parts[1],
            )
        }
    }

    companion object {
        const val SCHEME = "bearer"
    }
}
