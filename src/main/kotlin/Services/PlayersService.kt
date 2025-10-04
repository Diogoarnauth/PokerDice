import org.example.Domain.Players.Player
import org.springframework.stereotype.Service

@Service
class PlayersService(
    // Aqui idealmente teremos algo como:
    // private val playersRepository: PlayersRepository
) {

    // Simulação de auto-incremento para exemplo
    private var nextId = 1
    private val playersStorage = mutableMapOf<Int, Player>()

    fun createPlayer(
        username: String,
        password: String,
    ): Int {
        // Validações básicas
        require(username.isNotBlank()) { "Username cannot be blank" }
        require(password.length >= 6) { "Password must be at least 6 characters" }

        // Hash da password
        //val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())

        // Criar ‘player’
        val player = Player(
            id = nextId,
            token = null,
            username = username,
            password = password,
            name = "Player $nextId",
            age = 18,
            credit = 0,// saldo inicial, por exemplo
            winCounter = 0
        )

        // Guardar no "repository" (simulação)
        playersStorage[nextId] = player
        nextId++

        return player.id    }


    fun getPlayerById(id: Int): Player? {
        return playersStorage[id]
    }


}
