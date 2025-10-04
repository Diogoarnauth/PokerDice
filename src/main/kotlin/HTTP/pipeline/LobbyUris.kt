package org.example.HTTP.pipeline

import org.springframework.web.util.UriTemplate
import java.net.URI

object LobbyUris {
    const val PREFIX = "/api"
    fun home(): URI = URI(PREFIX)

    object Lobbies {
        // Paths (strings “declarativas”)
        const val ROOT = "$PREFIX/lobbies"
        const val LIST = ROOT                 // GET  -> listar lobbies abertos
        const val CREATE = ROOT               // POST -> criar lobby
        const val BY_ID = "$ROOT/{id}"        // GET  -> detalhes do lobby
        const val JOIN = "$ROOT/{id}/players" // POST -> entrar no lobby
        const val LEAVE_ME = "$ROOT/{id}/players/me" // DELETE -> sair

        // Helpers para criar URIs concretas
        fun byId(id: Int): URI =
            URI(UriTemplate(BY_ID).expand(id).toString())

        fun join(id: Int): URI =
            URI(UriTemplate(JOIN).expand(id).toString())

        fun leaveMe(id: Int): URI =
            URI(UriTemplate(LEAVE_ME).expand(id).toString())
    }
}
