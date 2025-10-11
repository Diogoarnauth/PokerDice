package pt.isel.daw.pokerDice.services


import kotlinx.datetime.Clock
import org.springframework.stereotype.Service
import pt.isel.daw.pokerDice.domain.lobbies.LobbiesDomain
import pt.isel.daw.pokerDice.domain.lobbies.Lobby
import pt.isel.daw.pokerDice.repository.TransactionManager

@Service
class LobbiesService(
    private val transactionManager: TransactionManager, // erro
    private val lobbiesDomain: LobbiesDomain,
    private val clock: Clock // erro
){

    /** Lista todos os lobbies visíveis (ainda não cheios) */
    fun getVisibleLobbies(): List<Lobby> = transactionManager.run {
        val lobbyRepository = it.lobbiesRepository
       return@run lobbyRepository.getLobbiesNotFull()
    }



    /** Detalhes de um lobby
    fun getById(id: Int): Lobby =
        transactionManager.run {
            val lobbiesRepository = it.lobbiesRepository  // ou lobbiesRepository, depende do nome no teu Transaction
            return@run lobbiesRepository.getById(id)
        }
    */

    /** Cria um novo Lobby */
   /* fun create (hostId: Int, input: LobbyCreateInputModel): CreateLobbyResult {
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
*/
}