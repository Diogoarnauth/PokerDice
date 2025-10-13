package pt.isel.daw.pokerDice.services


import kotlinx.datetime.Clock
import org.springframework.stereotype.Service
import pt.isel.daw.pokerDice.domain.lobbies.LobbiesDomain
import pt.isel.daw.pokerDice.domain.lobbies.Lobby
import pt.isel.daw.pokerDice.domain.players.PasswordValidationInfo
import pt.isel.daw.pokerDice.domain.players.PlayersDomain
import pt.isel.daw.pokerDice.repository.TransactionManager
import pt.isel.daw.pokerDice.utils.Either
import pt.isel.daw.pokerDice.utils.failure
import pt.isel.daw.pokerDice.utils.success

typealias CreateLobbyResult = Either<CreateLobbyError, Int>

sealed class CreateLobbyError {
    data object HostAlreadyHasAnOpenLobby : CreateLobbyError()
    data object HostAlreadyOnAnotherLobby : CreateLobbyError()
    data object InsecurePassword : CreateLobbyError()
    data object NotEnoughCredit : CreateLobbyError()
    data object CouldNotCreateLobby : CreateLobbyError()
    data object InvalidSettings : CreateLobbyError()
}

typealias GetLobbyResult = Either<LobbyGetByIdError, Lobby>

sealed class LobbyGetByIdError {
    data object LobbyNotFound : LobbyGetByIdError()
}

typealias JoinLobbyResult = Either<JoinLobbyError, Unit>
typealias LeaveLobbyResult = Either<LeaveLobbyError, Unit>

sealed class JoinLobbyError {
    data object LobbyNotFound : JoinLobbyError()
    data object LobbyFull : JoinLobbyError()
    data object AlreadyInLobby : JoinLobbyError()
    data object InsufficientCredits : JoinLobbyError()
}

sealed class LeaveLobbyError {
    data object LobbyNotFound : LeaveLobbyError()
    data object NotInLobby : LeaveLobbyError()
}



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



    /** Detalhes de um lobby*/
    fun getLobbyById(id: Int): GetLobbyResult =
    transactionManager.run {
    val repo = it.lobbiesRepository
    val lobby = repo.getById(id)
    if (lobby == null) failure(LobbyGetByIdError.LobbyNotFound)
    else success(lobby)
    }


    /** Cria um novo Lobby */
    fun createLobby(
        hostId: Int,
        name: String,
        description: String,
        isPrivate: Boolean,
        passwordValidationInfo: PasswordValidationInfo?,
        minPlayers: Int,
        maxPlayers: Int,
        rounds: Int,
        minCreditToParticipate: Int
    ): CreateLobbyResult {

        return transactionManager.run {
            val lobbyRepo = it.lobbiesRepository
            val playerRepo = it.playersRepository

            // Verificar se o host existe e buscar os seus créditos
            val host = playerRepo.getPlayerById(hostId)
                ?: return@run failure(CreateLobbyError.CouldNotCreateLobby)

            //  Verificar se o host já é dono de outro lobby
            if (lobbyRepo.existsByHost(hostId))
                return@run failure(CreateLobbyError.HostAlreadyHasAnOpenLobby)

            //  Verificar se o host já está noutro lobby
            if (host.lobbyId != null)
                return@run failure(CreateLobbyError.HostAlreadyOnAnotherLobby)

            //  Verificar se o host tem saldo suficiente
            if (host.credit < minCreditToParticipate)
                return@run failure(CreateLobbyError.NotEnoughCredit)

            // Verificar segurança da password se for privado
            if (isPrivate && (passwordValidationInfo == null || passwordValidationInfo.validationInfo.length < 8))
                return@run failure(CreateLobbyError.InsecurePassword)

            // Criar o lobby
            val lobbyId = lobbyRepo.createLobby(
                hostId,
                name,
                description,
                isPrivate,
                passwordValidationInfo,
                minPlayers,
                maxPlayers,
                rounds,
                minCreditToParticipate
            )

            if (lobbyId != null) success(lobbyId)
            else failure(CreateLobbyError.CouldNotCreateLobby)
        }
    }




    /** Entrar num lobby */
    fun joinLobby(lobbyId: Int, playerId: Int): JoinLobbyResult =
        transactionManager.run {
            val lobbiesRepo = it.lobbiesRepository
            val playersRepo = it.playersRepository

            // Verificar se o lobby existe
            val lobby = lobbiesRepo.getById(lobbyId)
                ?: return@run failure(JoinLobbyError.LobbyNotFound)

            // Verificar se o player existe
            val player = playersRepo.getPlayerById(playerId)
                ?: return@run failure(JoinLobbyError.LobbyNotFound)

            // Verificar se o player já está num lobby
            if (player.lobbyId != null)
                return@run failure(JoinLobbyError.AlreadyInLobby)

            //  Verificar se o lobby está cheio
            val currentPlayers = playersRepo.countPlayersInLobby(lobbyId)
            if (currentPlayers >= lobby.maxPlayers) // o maior nunca vai acontecer mas por precausão
                return@run failure(JoinLobbyError.LobbyFull)

            //  Verificar se o jogador tem créditos suficientes
            if (player.credit < lobby.minCreditToParticipate)
                return@run failure(JoinLobbyError.InsufficientCredits)

            // Adicionar jogador ao lobby
            playersRepo.updateLobbyIdForPlayer(playerId, lobbyId)

            success(Unit)
        }

    /*
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