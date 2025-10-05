import org.example.Domain.Players.Player
import org.gradle.internal.impldep.com.fasterxml.jackson.databind.util.Named

@Named
class PlayersService(
    private val playersDomain: Player,
) {
    fun createPlayer(
        username: String,
        password: String,
    ): Int {
        TODO()
    }


    fun getPlayerById(id: Int): Player? {
        TODO()
    }


}
