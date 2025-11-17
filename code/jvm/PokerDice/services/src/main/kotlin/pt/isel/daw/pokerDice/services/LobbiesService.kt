package pt.isel.daw.pokerDice.services

import kotlinx.datetime.Clock
import org.springframework.stereotype.Service
import pt.isel.daw.pokerDice.domain.lobbies.LobbiesDomain
import pt.isel.daw.pokerDice.domain.lobbies.Lobby
import pt.isel.daw.pokerDice.repository.TransactionManager
import pt.isel.daw.pokerDice.utils.Either
import pt.isel.daw.pokerDice.utils.failure
import pt.isel.daw.pokerDice.utils.success
import java.time.Duration

typealias CreateLobbyResult = Either<CreateLobbyError, Int>

sealed class CreateLobbyError {
    data object HostAlreadyHasAnOpenLobby : CreateLobbyError()

    // data object TimeToPlayInvalid : CreateLobbyError()

    data object HostAlreadyOnAnotherLobby : CreateLobbyError()

    data object TurnTimeInvalid : CreateLobbyError()

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

    data object LobbyAlreadyRunning : JoinLobbyError()
}

sealed class LeaveLobbyError {
    data object LobbyNotFound : LeaveLobbyError()

    data object NotInLobby : LeaveLobbyError()
}

typealias CloseLobbyResult = Either<CloseLobbyError, Unit>

sealed class CloseLobbyError {
    data object LobbyNotFound : CloseLobbyError()

    data object NotHost : CloseLobbyError()
}

@Service
class LobbiesService(
    private val transactionManager: TransactionManager,
    private val lobbiesDomain: LobbiesDomain,
    private val clock: Clock,
) {
    /** Lista todos os lobbies visíveis (ainda não cheios) */
    fun getVisibleLobbies(): List<Lobby> =
        transactionManager.run {
            val lobbyRepository = it.lobbiesRepository
            return@run lobbyRepository.getLobbiesNotFull()
        }

    fun leaveLobby(
        lobbyId: Int,
        userId: Int,
    ): LeaveLobbyResult =
        transactionManager.run {
            val lobbiesRepo = it.lobbiesRepository
            val usersRepo =
                it.usersRepository

            if (userId == lobbiesRepo.getById(lobbyId)?.hostId) {
                closeLobby(lobbyId, userId)
                return@run success(Unit)
            }

            // Verifica se o jogador pertence a este lobby
            val user =
                usersRepo.getUserById(userId)
                    ?: return@run failure(LeaveLobbyError.NotInLobby)

            if (user.lobbyId != lobbyId) {
                return@run failure(LeaveLobbyError.NotInLobby)
            }
            // TODO("SE FOR O HOST AO FAZER LEAVE DA KICK A TODOS NO LOBBY")
            // Remove o jogador do lobby (define lobby_id = NULL)
            usersRepo.updateLobbyIdForUser(userId, null)

            success(Unit)
        }

    /** Detalhes de um lobby*/
    fun getLobbyById(id: Int): GetLobbyResult =
        transactionManager.run {
            val repo = it.lobbiesRepository
            val lobby = repo.getById(id)
            println("<SERVICES> ${lobby!!.turnTime}")
            if (lobby == null) {
                failure(LobbyGetByIdError.LobbyNotFound)
            } else {
                success(lobby)
            }
        }

    /** Cria um novo Lobby */
    fun createLobby(
        hostId: Int,
        name: String,
        description: String,
        minUsers: Int,
        maxUsers: Int,
        rounds: Int,
        minCreditToParticipate: Int,
        turnTime: Duration,
    ): CreateLobbyResult {
        return transactionManager.run {
            val lobbyRepo = it.lobbiesRepository
            val userRepo = it.usersRepository

            // Verificar se o host existe e buscar os seus créditos
            val host =
                userRepo.getUserById(hostId)
                    ?: return@run failure(CreateLobbyError.CouldNotCreateLobby)

            //  Verificar se o host já é dono de outro lobby
            if (lobbyRepo.existsByHost(hostId)) {
                return@run failure(CreateLobbyError.HostAlreadyHasAnOpenLobby)
            }

            /*if (timeToPlay != 1.minutes && timeToPlay != 2.minutes) {
                return@run failure(CreateLobbyError.TimeToPlayInvalid)
            }*/

            //  Verificar se o host já está noutro lobby
            if (host.lobbyId != null) {
                return@run failure(CreateLobbyError.HostAlreadyOnAnotherLobby)
            }

            if (minUsers < 2) {
                return@run failure(CreateLobbyError.InvalidSettings)
            }

            if (maxUsers < minUsers) {
                return@run failure(CreateLobbyError.InvalidSettings)
            }

            if (minCreditToParticipate <= 0) {
                return@run failure(CreateLobbyError.InvalidSettings)
            }

            if (maxUsers > 10) {
                return@run failure(CreateLobbyError.InvalidSettings)
            }

            //  Verificar se o host tem saldo suficiente
            if (host.credit < minCreditToParticipate) {
                return@run failure(CreateLobbyError.NotEnoughCredit)
            }

            if (turnTime != Duration.ofMinutes(1) &&
                turnTime != Duration.ofMinutes(2) &&
                turnTime != Duration.ofMinutes(3) &&
                turnTime != Duration.ofMinutes(4) &&
                turnTime != Duration.ofMinutes(5)
            ) {
                return@run failure(CreateLobbyError.TurnTimeInvalid)
            }

                /* Verificar segurança da password se for privado
                if (isPrivate && (passwordValidationInfo == null || passwordValidationInfo.validationInfo.length < 8)) {
                    return@run failure(CreateLobbyError.InsecurePassword)
                }*/

            // Criar o lobby
            val lobbyId =
                lobbyRepo.createLobby(
                    hostId,
                    name,
                    description,
                    minUsers,
                    maxUsers,
                    rounds,
                    minCreditToParticipate,
                    turnTime,
                )

            // UPDATE LOBBY ID FOR USER
            userRepo.updateLobbyIdForUser(hostId, lobbyId)

            if (lobbyId != null) {
                success(lobbyId)
            } else {
                failure(CreateLobbyError.CouldNotCreateLobby)
            }
        }
    }

    /** Entrar num lobby */
    fun joinLobby(
        lobbyId: Int,
        userId: Int,
    ): JoinLobbyResult =
        transactionManager.run {
            val lobbiesRepo = it.lobbiesRepository
            val usersRepo = it.usersRepository

            // Verificar se o lobby existe
            val lobby =
                lobbiesRepo.getById(lobbyId)
                    ?: return@run failure(JoinLobbyError.LobbyNotFound)

            println("Lobby $lobby")

            if (lobby.isRunning) {
                return@run failure(JoinLobbyError.LobbyAlreadyRunning)
            }

            // Verificar se o user existe
            val user =
                usersRepo.getUserById(userId)
                    ?: return@run failure(JoinLobbyError.LobbyNotFound)

            // Verificar se o user já está num lobby
            if (user.lobbyId != null) {
                return@run failure(JoinLobbyError.AlreadyInLobby)
            }

            //  Verificar se o lobby está cheio
            val currentUsers = usersRepo.countUsersInLobby(lobbyId)
            println("currentUsers $currentUsers")
            if (currentUsers > lobby.maxUsers) {
                // o maior nunca vai acontecer mas por precausão
                return@run failure(JoinLobbyError.LobbyFull)
            }

            //  Verificar se o jogador tem créditos suficientes
            if (user.credit < lobby.minCreditToParticipate) {
                return@run failure(JoinLobbyError.InsufficientCredits)
            }

            // Adicionar jogador ao lobby
            usersRepo.updateLobbyIdForUser(userId, lobbyId)

            success(Unit)
        }

    fun closeLobby(
        lobbyId: Int,
        userId: Int,
    ): CloseLobbyResult =
        transactionManager.run {
            val lobbiesRepo = it.lobbiesRepository
            val usersRepo = it.usersRepository

            // Verificar se o lobby existe
            val lobby =
                lobbiesRepo.getById(lobbyId)
                    ?: return@run failure(CloseLobbyError.LobbyNotFound)

            // Verificar se o jogador é o host, está repetido mas pronto
            if (lobby.hostId != userId) {
                return@run failure(CloseLobbyError.NotHost)
            }

            // Remover todos os jogadores associados ao lobby
            usersRepo.clearLobbyForAllUsers(lobbyId)

            // Eliminar o lobby da base de dados
            lobbiesRepo.deleteLobbyById(lobbyId)

            success(Unit)
        }
}
