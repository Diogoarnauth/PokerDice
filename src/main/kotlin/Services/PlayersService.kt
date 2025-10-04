import org.example.Domain.Players.Player

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
