
package pt.isel.daw.pokerDice

import kotlinx.datetime.Clock
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.isel.daw.pokerDice.domain.lobbies.LobbiesDomain
import pt.isel.daw.pokerDice.domain.lobbies.LobbiesDomainConfig
import pt.isel.daw.pokerDice.domain.lobbies.Lobby
import pt.isel.daw.pokerDice.domain.users.User
import pt.isel.daw.pokerDice.domain.users.UsersDomain
import pt.isel.daw.pokerDice.repository.jdbi.JdbiTransactionManager
import pt.isel.daw.pokerDice.repository.jdbi.configureWithAppRequirements
import pt.isel.daw.pokerDice.services.CloseLobbyError
import pt.isel.daw.pokerDice.services.CreateLobbyError
import pt.isel.daw.pokerDice.services.CreatingAppInviteResult
import pt.isel.daw.pokerDice.services.JoinLobbyError
import pt.isel.daw.pokerDice.services.LeaveLobbyError
import pt.isel.daw.pokerDice.services.LobbiesService
import pt.isel.daw.pokerDice.services.LobbyGetByIdError
import pt.isel.daw.pokerDice.services.UsersService
import pt.isel.daw.pokerDice.utils.Either
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Testes do UsersService — versão adaptada do stor.
 * Usa a BD local de desenvolvimento e domínio real.
 */
class LobbyServicesTests {
    private lateinit var service: LobbiesService
    private lateinit var userService: UsersService

    private lateinit var username: String
    private lateinit var handle: Handle
    var adminId: Int = 0
    var userId: Int = 0
    var lobbyId: Int = 0
    lateinit var appInvite: CreatingAppInviteResult
    lateinit var appInviteString: String

    lateinit var appInvite2: CreatingAppInviteResult
    lateinit var appInviteString2: String

    // Setup inicial com BeforeEach
    @BeforeEach
    fun setup() {
        userService = createUserService()
        service = createLobbiesService()
        username = newUsername()

        // Admin related
        adminId =
            assertIs<Either.Right<Int>>(userService.bootstrapFirstUser(username, "Admin", 25, "StrongPass123")).value
        val admin: User = (assertIs<Either.Right<User>>(userService.getById(adminId))).value
        userService.deposit(1000, admin)

        // AppInvite related
        appInvite = userService.createAppInvite(adminId)
        val appInviteString = (assertIs<Either.Right<String>>(appInvite)).value

        // User related
        userId =
            assertIs<Either.Right<Int>>(
                userService.createUser(
                    "user1",
                    "utilizador",
                    25,
                    "passwordForte123",
                    appInviteString,
                ),
            ).value
        val user: User = (assertIs<Either.Right<User>>(userService.getById(userId))).value
        userService.deposit(1000, user)

        appInvite2 = userService.createAppInvite(userId)
        appInviteString2 = (assertIs<Either.Right<String>>(appInvite2)).value
        // Lobby related
        lobbyId = assertIs<Either.Right<Int>>(service.createLobby(adminId, "lobby", "Lobby teste", 3, 4, 5, 20)).value

        // Criação da instância de Jdbi
        val jdbi =
            Jdbi
                .create(
                    PGSimpleDataSource().apply {
                        setURL("jdbc:postgresql://localhost:5432/db?user=postgres&password=postgres")
                    },
                ).configureWithAppRequirements()

        handle = jdbi.open()

        handle.begin()
    }

    @AfterEach
    fun tearDown() {
        handle.rollback()

        handle.close()
    }

    // --- BOOTSTRAP TESTS ---
    @Nested
    inner class CreateLobbyTests {
        @Test
        fun `createLobby should succeed for valid parameters`() {
            // given
            val hostId = userId
            val name = "Test Lobby"
            val description = "This is a test lobby"
            val minUsers = 2
            val maxUsers = 5
            val rounds = 3
            val minCreditToParticipate = 100

            // when
            val result =
                service.createLobby(
                    hostId,
                    name,
                    description,
                    minUsers,
                    maxUsers,
                    rounds,
                    minCreditToParticipate,
                )
            println("result $result")
            // then

            val rightResult = assertIs<Either.Right<Int>>(result)
            println("rightResult $rightResult")
            assertTrue(rightResult.value > 0)
        }

        @Test
        fun `createLobby should fail if host does not exist`() {
            // given
            val hostId = 999 // Assuming this host doesn't exist
            val name = "Test Lobby"
            val description = "This is a test lobby"
            val minUsers = 2
            val maxUsers = 5
            val rounds = 3
            val minCreditToParticipate = 100

            // when
            val result =
                service.createLobby(
                    hostId,
                    name,
                    description,
                    minUsers,
                    maxUsers,
                    rounds,
                    minCreditToParticipate,
                )

            // then
            val leftResult = assertIs<Either.Left<CreateLobbyError>>(result)
            assertEquals(CreateLobbyError.CouldNotCreateLobby, leftResult.value) // Host not found
        }

        @Test
        fun `createLobby should fail if host already has an open lobby`() {
            // when
            service.createLobby(
                userId,
                "Test Lobby",
                "This is a test lobby",
                2,
                5,
                3,
                10,
            )

            // given
            val hostId = userId
            val name = "Test Lobby"
            val description = "This is a test lobby"
            val minUsers = 2
            val maxUsers = 5
            val rounds = 3
            val minCreditToParticipate = 100

            // when
            val result =
                service.createLobby(
                    hostId,
                    name,
                    description,
                    minUsers,
                    maxUsers,
                    rounds,
                    minCreditToParticipate,
                )

            // then
            val leftResult = assertIs<Either.Left<CreateLobbyError>>(result)
            assertEquals(CreateLobbyError.HostAlreadyHasAnOpenLobby, leftResult.value)
        }

        @Test
        fun `createLobby should fail if settings are invalid minUsers smaller 2`() {
            // given
            val hostId = userId
            val name = "Test Lobby"
            val description = "This is a test lobby"
            val minUsers = 1
            val maxUsers = 5
            val rounds = 3
            val minCreditToParticipate = 100

            // when
            val result =
                service.createLobby(
                    hostId,
                    name,
                    description,
                    minUsers,
                    maxUsers,
                    rounds,
                    minCreditToParticipate,
                )

            // then
            val leftResult = assertIs<Either.Left<CreateLobbyError>>(result)
            assertEquals(CreateLobbyError.InvalidSettings, leftResult.value) // Invalid settings
        }

        @Test
        fun `createLobby should fail if maxUsers smaller than minUsers`() {
            // given
            val hostId = userId
            val name = "Test Lobby"
            val description = "This is a test lobby"
            val minUsers = 3
            val maxUsers = 2
            val rounds = 3
            val minCreditToParticipate = 100

            // when
            val result =
                service.createLobby(
                    hostId,
                    name,
                    description,
                    minUsers,
                    maxUsers,
                    rounds,
                    minCreditToParticipate,
                )

            // then
            val leftResult = assertIs<Either.Left<CreateLobbyError>>(result)
            assertEquals(CreateLobbyError.InvalidSettings, leftResult.value) // Invalid settings
        }

        @Test
        fun `createLobby should fail if minCreditToParticipate smaller or equal than 0`() {
            // given
            val hostId = userId
            val name = "Test Lobby"
            val description = "This is a test lobby"
            val minUsers = 2
            val maxUsers = 5
            val rounds = 3
            val minCreditToParticipate = 0 // Invalid credit

            // when
            val result =
                service.createLobby(
                    hostId,
                    name,
                    description,
                    minUsers,
                    maxUsers,
                    rounds,
                    minCreditToParticipate,
                )

            // then
            val leftResult = assertIs<Either.Left<CreateLobbyError>>(result)
            assertEquals(CreateLobbyError.InvalidSettings, leftResult.value) // Invalid settings
        }

        @Test
        fun `createLobby should fail if maxUsers smaller than 10`() {
            // given
            val hostId = userId
            val name = "Test Lobby"
            val description = "This is a test lobby"
            val minUsers = 2
            val maxUsers = 11 // Invalid maxUsers > 10
            val rounds = 3
            val minCreditToParticipate = 100

            // when
            val result =
                service.createLobby(
                    hostId,
                    name,
                    description,
                    minUsers,
                    maxUsers,
                    rounds,
                    minCreditToParticipate,
                )

            // then
            val leftResult = assertIs<Either.Left<CreateLobbyError>>(result)
            assertEquals(CreateLobbyError.InvalidSettings, leftResult.value) // Invalid settings
        }

        @Test
        fun `createLobby should fail if host has insufficient credit`() {
            // given
            val hostId = userId
            val name = "Test Lobby"
            val description = "This is a test lobby"
            val minUsers = 2
            val maxUsers = 5
            val rounds = 3
            val minCreditToParticipate = 10000 // Host has less credit than required

            // when
            val result =
                service.createLobby(
                    hostId,
                    name,
                    description,
                    minUsers,
                    maxUsers,
                    rounds,
                    minCreditToParticipate,
                )

            // then
            val leftResult = assertIs<Either.Left<CreateLobbyError>>(result)
            assertEquals(CreateLobbyError.NotEnoughCredit, leftResult.value)
        }
    }

    // --- GET VISIBLE LOBBIES TESTS ---
    @Nested
    inner class GetVisibleLobbiesTests {
        @Test
        fun `getVisibleLobbies should return non-empty list if there are visible lobbies`() { // failed
            // given
            val hostId = adminId
            val name = "Test Lobby"
            val description = "This is a test lobby"
            val minUsers = 2
            val maxUsers = 5
            val rounds = 3
            val minCreditToParticipate = 100

            // Cria um lobby que não está cheio
            service.createLobby(
                hostId,
                name,
                description,
                minUsers,
                maxUsers,
                rounds,
                minCreditToParticipate,
            )

            // when
            val result = service.getVisibleLobbies()

            // then
            assertTrue(result.isNotEmpty(), "A lista de lobbies não deve estar vazia")
            assertTrue(result.all { it.maxUsers > it.minUsers }, "Todos os lobbies devem estar com vagas disponíveis")
        }

        @Test
        fun `getVisibleLobbies should return empty list if no lobbies are visible`() { // failed
            // given
            // Nenhum lobby foi criado ainda

            // when
            val result = service.getVisibleLobbies()

            // then
            assertTrue(result.isEmpty(), "A lista de lobbies deve estar vazia")
        }

            /*@Test   Aqui falta criar user e depois mete-los no lobby
            fun `getVisibleLobbies should return only non-full lobbies`() {
                // given

                val hostId = adminId
                val name1 = "Lobby 1"
                val description1 = "This is the first lobby"
                val minUsers = 2
                val maxUsers = 3
                val rounds = 3
                val minCreditToParticipate = 100

                // Cria o primeiro lobby (cheio)
                service.createLobby(
                    hostId,
                    name1,
                    description1,
                    minUsers,
                    maxUsers,
                    rounds,
                    minCreditToParticipate,
                )

                val name2 = "Lobby 2"
                val description2 = "This is the second lobby"
                val maxUsers2 = 5 // Não cheio
                // Cria o segundo lobby (não cheio)
                service.createLobby(
                    hostId,
                    name2,
                    description2,
                    minUsers,
                    maxUsers2,
                    rounds,
                    minCreditToParticipate,
                )

                // when
                val result = service.getVisibleLobbies()

                // then
                assertEquals(1, result.size, "Deve retornar apenas lobbies não cheios")
                assertTrue(result[0].maxUsers > result[0].minUsers, "O lobby retornado não deve estar cheio")
            }

             */
    }

    @Nested
    inner class LeaveLobbyTests {
        @Test
        fun `leaveLobby should succeed if player is not host and belongs to lobby`() {
            // when
            service.joinLobby(lobbyId, userId)

            val result = service.leaveLobby(lobbyId, userId)

            // then
            assertIs<Either.Right<Unit>>(result)
        }

        @Test
        fun `leaveLobby should fail if player does not belong to lobby`() {
            // given
            val nonMemberUserId = 999 // Usuário que não pertence ao lobby

            // when
            val result = service.leaveLobby(lobbyId, nonMemberUserId)

            // then
            val leftResult = assertIs<Either.Left<LeaveLobbyError>>(result)
            assertEquals(LeaveLobbyError.NotInLobby, leftResult.value) // Player is not in the lobby
        }

        @Test
        fun `leaveLobby should close the lobby if host leaves`() { // failed
            // when
            val result = service.leaveLobby(lobbyId, adminId)
            // then
            assertIs<Either.Right<Unit>>(result)

            // Verificar que o lobby foi fechado
            val lobby = service.getVisibleLobbies().find { it.id == lobbyId }

            assertNull(lobby, "O lobby deve ser fechado quando o host sai.")
        }

        @Test
        fun `leaveLobby should fail if the lobby is already closed (host left and lobby closed)`() {
            // given
            service.leaveLobby(lobbyId, adminId) // Fechar o lobby

            // when
            val result = service.leaveLobby(lobbyId, userId)

            // then
            val leftResult = assertIs<Either.Left<LeaveLobbyError>>(result)
            assertEquals(LeaveLobbyError.NotInLobby, leftResult.value) // O jogador não pode sair de um lobby já fechado
        }

        @Test
        fun `leaveLobby should fail if user is not in the lobby (user trying to leave the same lobby multiple times)`() {
            // given
            service.leaveLobby(lobbyId, userId)

            // when
            val result = service.leaveLobby(lobbyId, userId)

            // then
            val leftResult = assertIs<Either.Left<LeaveLobbyError>>(result)
            assertEquals(LeaveLobbyError.NotInLobby, leftResult.value)
        }
    }

    // --- GET LOBBY BY ID TESTS ---
    @Nested
    inner class GetLobbyByIdTests {
        @Test
        fun `getLobbyById should return lobby when it exists`() {
            // when
            val result = service.getLobbyById(lobbyId)

            // then
            val rightResult = assertIs<Either.Right<Lobby>>(result)
            assertEquals(lobbyId, rightResult.value.id)
            assertNotNull(rightResult.value.name, "O lobby deve ter um nome válido")
        }

        @Test
        fun `getLobbyById should fail if lobby does not exist`() {
            // given
            val nonExistentLobbyId = 99999 // ID que não existe

            // when
            val result = service.getLobbyById(nonExistentLobbyId)

            // then
            val leftResult = assertIs<Either.Left<LobbyGetByIdError>>(result)
            assertEquals(LobbyGetByIdError.LobbyNotFound, leftResult.value) // Lobby não encontrado
        }
    }

    @Nested
    inner class JoinLobbyTests {
        @Test
        fun `joinLobby should succeed when user can join`() {
            // given
            val userToJoin = userId // Usuário já criado no setup
            val lobby = service.getLobbyById(lobbyId) // Lobby já criado no setup

            // when
            val result = service.joinLobby(lobbyId, userToJoin)

            // then
            assertIs<Either.Right<Unit>>(result) // Espera que o resultado seja um sucesso
        }

        @Test
        fun `joinLobby should fail if lobby does not exist`() {
            // given
            val nonExistentLobbyId = 99999 // ID que não existe
            val userToJoin = userId

            // when
            val result = service.joinLobby(nonExistentLobbyId, userToJoin)

            // then
            val leftResult = assertIs<Either.Left<JoinLobbyError>>(result)
            assertEquals(JoinLobbyError.LobbyNotFound, leftResult.value)
        }

        @Test
        fun `joinLobby should fail if user is already in a lobby`() {
            // given
            // Vamos garantir que o usuário esteja já no lobby, com base no setup
            val userToJoin = userId

            // Quando o usuário já estiver no lobby, tentamos juntá-lo de novo
            val preSet = service.joinLobby(lobbyId, userToJoin)

            val result = service.joinLobby(lobbyId, userToJoin)
            println("result $result")

            // then
            val leftResult = assertIs<Either.Left<JoinLobbyError>>(result)
            assertEquals(JoinLobbyError.AlreadyInLobby, leftResult.value)
        }

        @Test
        fun `joinLobby should fail if lobby is full`() {
            // SETUP USER 1
            val newUser1 = userService.createUser("user2", "User Two", 25, "SecurePass123", appInviteString2)
            val newUserId1 = assertIs<Either.Right<Int>>(newUser1).value
            val user1: User = (assertIs<Either.Right<User>>(userService.getById(newUserId1))).value

            userService.deposit(1000, user1)

            service.joinLobby(lobbyId, newUserId1)

            val appInvite3 = userService.createAppInvite(newUserId1)
            val appInviteString3 = (assertIs<Either.Right<String>>(appInvite3)).value

            // SETUP USER 2

            val newUser2 = userService.createUser("user3", "User three", 25, "SecurePass123", appInviteString3)
            val newUserId2 = assertIs<Either.Right<Int>>(newUser2).value
            val user2: User = (assertIs<Either.Right<User>>(userService.getById(newUserId2))).value

            userService.deposit(1000, user2)

            service.joinLobby(lobbyId, newUserId2)

            val appInvite4 = userService.createAppInvite(newUserId1)
            val appInviteString4 = (assertIs<Either.Right<String>>(appInvite4)).value

            // SETUP USER 3
            val newUser3 = userService.createUser("user4", "User four", 25, "SecurePass123", appInviteString4)
            val newUserId3 = assertIs<Either.Right<Int>>(newUser3).value
            val user3: User = (assertIs<Either.Right<User>>(userService.getById(newUserId3))).value

            userService.deposit(1000, user3)

            service.joinLobby(lobbyId, newUserId3)

            val appInvite5 = userService.createAppInvite(newUserId1)
            val appInviteString5 = (assertIs<Either.Right<String>>(appInvite5)).value

            // SETUP USER 4
            val newUser4 = userService.createUser("user5", "User five", 25, "SecurePass123", appInviteString5)
            val newUserId4 = assertIs<Either.Right<Int>>(newUser4).value
            val user4: User = (assertIs<Either.Right<User>>(userService.getById(newUserId4))).value

            userService.deposit(1000, user4)

            service.joinLobby(lobbyId, newUserId4)

            val appInvite6 = userService.createAppInvite(newUserId1)
            val appInviteString6 = (assertIs<Either.Right<String>>(appInvite6)).value

            // SETUP USER 5
            val newUser5 = userService.createUser("user6", "User five", 25, "SecurePass123", appInviteString6)
            val newUserId5 = assertIs<Either.Right<Int>>(newUser5).value
            val user5: User = (assertIs<Either.Right<User>>(userService.getById(newUserId5))).value

            userService.deposit(1000, user5)

            val result = service.joinLobby(lobbyId, newUserId5)

            // then
            val leftResult = assertIs<Either.Left<JoinLobbyError>>(result)
            assertEquals(JoinLobbyError.LobbyFull, leftResult.value)
        }

        @Test
        fun `joinLobby should fail if user has insufficient credits`() {
            // given
            val userIdWithLowCredits =
                userService.createUser("userLowCredit", "User Low Credit", 25, "LowPass123", appInviteString2).let {
                    assertIs<Either.Right<Int>>(it).value
                }
            val userRight = userService.getById(userIdWithLowCredits)
            val user = assertIs<Either.Right<User>>(userRight).value

            val userToJoin = userIdWithLowCredits
            val lobby = service.getLobbyById(lobbyId)

            // Supondo que o usuário criado não tenha créditos suficientes
            userService.deposit(-1000, user) // Removemos os créditos do usuário

            // when
            val result = service.joinLobby(lobbyId, userToJoin)

            // then
            val leftResult = assertIs<Either.Left<JoinLobbyError>>(result)
            assertEquals(JoinLobbyError.InsufficientCredits, leftResult.value)
        }
    }

    @Nested
    inner class CloseLobbyTests {
        @Test
        fun `closeLobby succeeds when host closes existing lobby`() {
            // when
            val result = service.closeLobby(lobbyId = 1, userId = adminId)

            // then
            assertIs<Either.Right<Unit>>(result)
        }

        @Test
        fun `closeLobby should fail if lobby does not exist`() {
            // given
            val fakeLobbyId = 99999

            // when
            val result = service.closeLobby(fakeLobbyId, adminId)

            // then
            val leftResult = assertIs<Either.Left<CloseLobbyError>>(result)
            assertEquals(CloseLobbyError.LobbyNotFound, leftResult.value)
        }

        @Test
        fun `closeLobby should fail if user is not the host`() {
            // when
            val result = service.closeLobby(lobbyId, userId)

            // then
            val leftResult = assertIs<Either.Left<CloseLobbyError>>(result)
            assertEquals(CloseLobbyError.NotHost, leftResult.value)
        }

        @Test
        fun `closeLobby should remove all players from the lobby`() {
            // given
            service.joinLobby(lobbyId, userId)

            // when
            val result = service.closeLobby(lobbyId, adminId)

            // then
            assertIs<Either.Right<Unit>>(result)

            // Verifica que o lobby foi eliminado
            val lobbyCheck = service.getLobbyById(lobbyId)
            val leftLobby = assertIs<Either.Left<LobbyGetByIdError>>(lobbyCheck)
            assertEquals(LobbyGetByIdError.LobbyNotFound, leftLobby.value)

            // Verifica que o jogador foi removido do lobby
            val updatedUser = assertIs<Either.Right<User>>(userService.getById(userId)).value
            assertNull(updatedUser.lobbyId, "O jogador deve ter sido removido do lobby")
        }
    }

    // Funções auxiliares
    companion object {
        private val jdbi =
            Jdbi
                .create(
                    PGSimpleDataSource().apply {
                        setURL("jdbc:postgresql://localhost:5432/db?user=postgres&password=postgres")
                    },
                ).configureWithAppRequirements()

        private fun createLobbiesService(clock: Clock = Clock.System): LobbiesService {
            val lobbiesDomain =
                LobbiesDomain(
                    LobbiesDomainConfig(
                        minUsersAllowed = 2,
                        maxUsersAllowed = 6,
                        minRoundsAllowed = 2,
                        maxRoundsAllowed = 10,
                        minCreditAllowed = 10,
                    ),
                    BCryptPasswordEncoder(),
                )
            return LobbiesService(
                transactionManager = JdbiTransactionManager(jdbi),
                lobbiesDomain = lobbiesDomain,
                clock = clock,
            )
        }

        private fun createUserService(clock: Clock = Clock.System): UsersService {
            val usersDomain =
                UsersDomain(
                    passwordEncoder =
                        org.springframework.security.crypto.bcrypt
                            .BCryptPasswordEncoder(),
                    tokenEncoder =
                        pt.isel.daw.pokerDice.domain.users
                            .Sha256TokenEncoder(),
                    config =
                        pt.isel.daw.pokerDice.domain.users.UsersDomainConfig(
                            tokenSizeInBytes = 256 / 8,
                            tokenTtl = kotlin.time.Duration.parse("30d"),
                            tokenRollingTtl = kotlin.time.Duration.parse("30m"),
                            maxTokensPerUser = 3,
                            minUsernameLength = 3,
                            minPasswordLength = 6,
                            minAge = 18,
                            maxAge = 120,
                        ),
                )

            val inviteDomain =
                pt.isel.daw.pokerDice.domain.invite.InviteDomain(
                    inviteEncoder =
                        pt.isel.daw.pokerDice.domain.invite
                            .Sha256InviteEncoder(),
                    config =
                        pt.isel.daw.pokerDice.domain.invite.InviteDomainConfig(
                            expireInviteTime = kotlin.time.Duration.parse("60m"),
                            validState = "pending",
                            expiredState = "expired",
                            usedState = "used",
                            declinedState = "DECLINED",
                        ),
                )

            return UsersService(
                transactionManager = JdbiTransactionManager(jdbi),
                userDomain = usersDomain,
                inviteDomain = inviteDomain,
                clock = clock,
            )
        }

        private fun newUsername() = "user-${Random.nextInt(1_000_000)}"
    }
}
