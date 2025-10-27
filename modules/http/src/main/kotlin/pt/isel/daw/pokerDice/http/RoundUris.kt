package pt.isel.daw.pokerDice.http

import org.springframework.web.util.UriTemplate
import java.net.URI

object RoundUris {
    const val PREFIX = "/api"

    object Rounds {
        // Paths “declarativos”
        const val ROOT = "$PREFIX/game/{gameId}/round"
        const val START = ROOT // POST -> start a new round
        const val BY_ID = "$PREFIX/rounds/{roundId}" // GET -> detalhes da round
        const val END = "$PREFIX/rounds/{roundId}/end" // POST -> terminar a round

        // Helpers para criar URIs concretas
        fun start(gameId: Int): URI = URI(UriTemplate(START).expand(gameId).toString())

        fun byId(roundId: Int): URI = URI(UriTemplate(BY_ID).expand(roundId).toString())

        fun end(roundId: Int): URI = URI(UriTemplate(END).expand(roundId).toString())
    }
}
