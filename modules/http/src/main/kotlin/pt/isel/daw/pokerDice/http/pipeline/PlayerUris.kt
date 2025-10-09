package pt.isel.daw.pokerDice.http.pipeline


import org.springframework.web.util.UriTemplate
import java.net.URI

object PlayerUris {
    const val PREFIX = "/api"
    const val HOME = PREFIX

    fun home(): URI = URI(HOME)

    object Players {
        const val CREATE = "$PREFIX/players"
        const val TOKEN = "$PREFIX/players/token"
        const val LOGOUT = "$PREFIX/logout"
        const val GET_BY_ID = "$PREFIX/players/{id}"
        const val HOME = "$PREFIX/me"

        fun byId(id: Int): URI = UriTemplate(GET_BY_ID).expand(id)

        fun home(): URI = URI(HOME)

        fun login(): URI = URI(TOKEN)

        fun register(): URI = URI(CREATE)
    }

  /*  object Status {
        const val HOSTNAME = "$PREFIX/status/hostname"
        const val IP = "$PREFIX/status/ip"
    }

    object Chat {
        const val LISTEN = "$PREFIX/chat/listen"
        const val SEND = "$PREFIX/chat/send"
    }*/
}