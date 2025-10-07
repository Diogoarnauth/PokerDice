import org.example.Domain.Players.Player
import org.example.HTTP.model.LobbyCreateInputModel
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger


@Service
class LobbiesService(
    // Dependências futuras, ex: playersRepository, transactionManager
){
    private val nextLobbyId = AtomicInteger(1)
    private val lobbies = mutableMapOf<Int, Lobby>()

    /** Lista todos os lobbies visíveis (ainda não cheios) */
    fun listOpen(): List<Lobby> =
        lobbies.values.filter { !it.isFull }

    /** Cria um novo Lobby */
    fun create (hostId: Int, input: LobbyCreateInputModel): CreateLobbyResult{
        // validações básicas
        if (input.minPlayers <= 0 || input.maxPlayers < input.minPlayers || input.nRounds <= 0) {
            return CreateLobbyResult.InvalidSettings
        }

        val lobbyId = nextLobbyId.getAndIncrement()
        val lobby = Lobby(
            id = lobbyId,
            name = input.name,
            description = input.description,
            hostId = hostId,
            minPlayers = input.minPlayers,
            maxPlayers = input.maxPlayers,
            rounds = input.nRounds,
            players = mutableListOf() // lista de jogadores vazia
        )




        // Adiciona host como primeiro jogador
        val hostPlayer = Player(id = hostId, username = "placeholder", password = "",name= "Renata", age = 18 ,
            credit = 100, winCounter = 0)
        lobby.players.add(hostPlayer)

        lobbies[lobbyId] = lobby
        return CreateLobbyResult.Ok(lobbyId)
    }
    /** Detalhes de um lobby */
    fun getById(id: Int): GetLobbyResult =
        lobbies[id]?.let {
            GetLobbyResult.Ok(
                id = it.id,
                name = it.name,
                description = it.description,
                hostId = it.hostId,
                players = it.players.toList(),
                minPlayers = it.minPlayers,
                maxPlayers = it.maxPlayers,
                rounds = it.rounds
            )
        } ?: GetLobbyResult.NotFound

    /** Entrar num lobby */
    fun join(lobbyId: Int, playerId: Int): JoinLobbyResult {
        val lobby = lobbies[lobbyId] ?: return JoinLobbyResult.NotFound

        if (lobby.players.any { it.id == playerId }) return JoinLobbyResult.AlreadyIn
        if (lobby.isFull) return JoinLobbyResult.Full

        val player = Player(id = playerId, username = "placeholder", password = "",name= "Renata", age = 18 ,
            credit = 100, winCounter = 0)
        lobby.players.add(player)
        return JoinLobbyResult.Ok
    }

    /** Sair de um lobby */
    fun leave(lobbyId: Int, playerId: Int): LeaveLobbyResult {
        val lobby = lobbies[lobbyId] ?: return LeaveLobbyResult.NotFound
        val player = lobby.players.find { it.id == playerId } ?: return LeaveLobbyResult.NotInLobby

        lobby.players.remove(player)

        // Se o host sair e o lobby não começou, fecha o lobby
        if (player.id == lobby.hostId && lobby.players.isEmpty()) {
            lobbies.remove(lobbyId)
        }

        return LeaveLobbyResult.Ok
    }

}