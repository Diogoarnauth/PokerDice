package pt.isel.daw.pokerDice.http

import org.springframework.web.util.UriTemplate
import java.net.URI

object Uris {
    const val PREFIX = "/api"

    // User URIs
    object Users {
        const val CREATE = "$PREFIX/users"
        const val TOKEN = "$PREFIX/users/token"
        const val LOGOUT = "$PREFIX/logout"
        const val GET_BY_ID = "$PREFIX/users/{id}"
        const val HOME = "$PREFIX/me"
        const val INVITE = "$PREFIX/invite"
        const val DEPOSIT = "$PREFIX/deposit"
        const val BOOTSTRAP = "$PREFIX/bootstrap"
        const val CHECK_ADMIN = "$PREFIX/checkAdmin"
        const val GETPLAYERSONLOBBY = "$PREFIX/users/lobby/{id}"
        const val GETME = "$PREFIX/users/getMe"
        const val LISTEN = "$PREFIX/users/listen"

        fun byId(id: Int): URI = UriTemplate(GET_BY_ID).expand(id)

        fun home(): URI = URI(HOME)

        fun login(): URI = URI(TOKEN)

        fun register(): URI = URI(CREATE)
    }

    // Game URIs
    object Games {
        const val ROOT = "$PREFIX/games"
        const val BY_ID = "$ROOT/{gameId}"
        const val START = "$ROOT/{lobbyId}/start"
        const val ROLL = "$ROOT/{lobbyId}/roll"
        const val REROLL = "$ROOT/{lobbyId}/reroll"
        const val LEAVE = "$ROOT/{gameId}/leave"
        const val END_TURN = "$ROOT/{gameId}/end"
        const val END_GAME = "$ROOT/{gameId}/end-game"
        const val STATUS = "$ROOT/{gameId}/status"
        const val RESULT = "$ROOT/{gameId}/final-result"
        const val PLAYER_TURN = "$ROOT/{gameId}/player-turn"
        const val GETGAME = "$ROOT/lobby/{id}"

        fun byId(gameId: String): URI = URI(UriTemplate(BY_ID).expand(gameId).toString())

        fun start(lobbyId: Int): URI = URI(UriTemplate(START).expand(lobbyId).toString())

        fun roll(gameId: String): URI = URI(UriTemplate(ROLL).expand(gameId).toString())

        fun reroll(gameId: String): URI = URI(UriTemplate(REROLL).expand(gameId).toString())

        fun leave(gameId: String): URI = URI(UriTemplate(LEAVE).expand(gameId).toString())

        fun endTurn(gameId: String): URI = URI(UriTemplate(END_TURN).expand(gameId).toString())

        fun status(gameId: String): URI = URI(UriTemplate(STATUS).expand(gameId).toString())

        fun finalResult(gameId: String): URI = URI(UriTemplate(RESULT).expand(gameId).toString())
    }

    // Lobby URIs
    object Lobbies {
        const val ROOT = "$PREFIX/lobbies"
        const val LIST = ROOT // GET  -> listar lobbies abertos
        const val CREATE = ROOT // POST -> criar lobby
        const val BY_ID = "$ROOT/{id}" // GET  -> detalhes do lobby
        const val JOIN = "$ROOT/{id}/users" // POST -> entrar no lobby
        const val LEAVE_ME = "$ROOT/{id}/leave" // DELETE -> sair

        fun byId(id: Int): URI = URI(UriTemplate(BY_ID).expand(id).toString())

        fun join(id: Int): URI = URI(UriTemplate(JOIN).expand(id).toString())

        fun leaveMe(id: Int): URI = URI(UriTemplate(LEAVE_ME).expand(id).toString())
    }

    // Status URIs
    object Status {
        const val HOSTNAME = "$PREFIX/status/hostname"
        const val IP = "$PREFIX/status/ip"
    }
}
