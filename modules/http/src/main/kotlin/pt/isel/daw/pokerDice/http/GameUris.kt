package pt.isel.daw.pokerDice.http

import org.springframework.web.util.UriTemplate
import java.net.URI

object GameUris {
    const val PREFIX = "/api"

    object Games {
        const val ROOT = "$PREFIX/games"
        const val BY_ID = "$ROOT/{gameId}"
        const val START = "$ROOT/{lobbyId}/start"
        const val ROLL = "$ROOT/{gameId}/roll"
        const val REROLL = "$ROOT/{gameId}/reroll"
        const val END_TURN = "$ROOT/{gameId}/end-turn"
        const val STATUS = "$ROOT/{gameId}/status"
        const val RESULT = "$ROOT/{gameId}/final-result"

        fun byId(gameId: String): URI = URI(UriTemplate(BY_ID).expand(gameId).toString())

        fun start(lobbyId: Int): URI = URI(UriTemplate(START).expand(lobbyId).toString())

        fun roll(gameId: String): URI = URI(UriTemplate(ROLL).expand(gameId).toString())

        fun reroll(gameId: String): URI = URI(UriTemplate(REROLL).expand(gameId).toString())

        fun endTurn(gameId: String): URI = URI(UriTemplate(END_TURN).expand(gameId).toString())

        fun status(gameId: String): URI = URI(UriTemplate(STATUS).expand(gameId).toString())

        fun finalResult(gameId: String): URI = URI(UriTemplate(RESULT).expand(gameId).toString())
    }
}
