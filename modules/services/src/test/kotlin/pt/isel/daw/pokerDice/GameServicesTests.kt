package pt.isel.daw.pokerDice

import kotlinx.datetime.Clock
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.isel.daw.pokerDice.domain.games.Game
import pt.isel.daw.pokerDice.domain.games.GameDomain
import pt.isel.daw.pokerDice.domain.invite.InviteDomain
import pt.isel.daw.pokerDice.domain.invite.InviteDomainConfig
import pt.isel.daw.pokerDice.domain.invite.Sha256InviteEncoder
import pt.isel.daw.pokerDice.domain.lobbies.LobbiesDomain
import pt.isel.daw.pokerDice.domain.lobbies.LobbiesDomainConfig
import pt.isel.daw.pokerDice.domain.lobbies.Lobby
import pt.isel.daw.pokerDice.domain.users.Sha256TokenEncoder
import pt.isel.daw.pokerDice.domain.users.User
import pt.isel.daw.pokerDice.domain.users.UsersDomain
import pt.isel.daw.pokerDice.domain.users.UsersDomainConfig
import pt.isel.daw.pokerDice.repository.jdbi.JdbiTransactionManager
import pt.isel.daw.pokerDice.repository.jdbi.configureWithAppRequirements
import pt.isel.daw.pokerDice.services.GameCreationError
import pt.isel.daw.pokerDice.services.GameError
import pt.isel.daw.pokerDice.services.GameService
import pt.isel.daw.pokerDice.services.LobbiesService
import pt.isel.daw.pokerDice.services.UsersService
import pt.isel.daw.pokerDice.utils.Either
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration

class GameServicesTests {
    private lateinit var gameService: GameService
    private lateinit var userService: UsersService
    private lateinit var lobbyService: LobbiesService

    private lateinit var handle: Handle
    private lateinit var username: String

    private var adminId: Int = 0
    private var userId: Int = 0
    private var lobbyId: Int = 0
    private lateinit var appInviteString: String

    @BeforeEach
    fun setup() {
        userService = createUserService()
        lobbyService = createLobbiesService()
        gameService = createGameService()

        username = newUsername()

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

        // Admin setup
        adminId =
            assertIs<Either.Right<Int>>(userService.bootstrapFirstUser(username, "Admin", 25, "StrongPass123")).value
        val admin: User = (assertIs<Either.Right<User>>(userService.getById(adminId))).value
        userService.deposit(1000, admin)

        // AppInvite setup
        val appInvite = userService.createAppInvite(adminId)
        appInviteString = (assertIs<Either.Right<String>>(appInvite)).value

        // Create user
        userId =
            assertIs<Either.Right<Int>>(
                userService.createUser(
                    "user1",
                    "Utilizador",
                    25,
                    "passwordForte123",
                    appInviteString,
                ),
            ).value
        val user: User = (assertIs<Either.Right<User>>(userService.getById(userId))).value
        userService.deposit(1000, user)

        // Create Lobby
        lobbyId =
            assertIs<Either.Right<Int>>(
                lobbyService.createLobby(
                    adminId,
                    "Lobby de Teste",
                    "Lobby para teste de game",
                    2,
                    5,
                    3,
                    50,
                ),
            ).value

        // Join user to lobby
        lobbyService.joinLobby(lobbyId, userId)
    }

    @AfterEach
    fun tearDown() {
        handle.rollback()
        handle.close()
    }

    // -------------------------------------------------------------------------
    // TESTES PARA CREATE GAME
    // -------------------------------------------------------------------------
    @Nested
    inner class CreateGameTests {
        @Test
        fun `createGame should succeed when host starts game with enough players`() {
            // when
            val result = gameService.createGame(adminId, lobbyId)

            // then
            val right = assertIs<Either.Right<Int?>>(result)
            assertNotNull(right.value)
            assertTrue(right.value!! > 0)
        }

        @Test
        fun `createGame should fail if lobby does not exist`() {
            // when
            val result = gameService.createGame(adminId, 99999)

            // then
            val left = assertIs<Either.Left<GameCreationError>>(result)
            assertTrue(left.value is GameCreationError.LobbyNotFound)
        }

        @Test
        fun `createGame should fail if user is not the host`() { // Failed
            // when
            val result = gameService.createGame(userId, lobbyId)

            // then
            val left = assertIs<Either.Left<GameCreationError>>(result)
            assertTrue(left.value is GameCreationError.NotTheHost)
        }

        @Test
        fun `createGame should fail if game already running`() {
            // given
            val first = gameService.createGame(adminId, lobbyId)
            assertIs<Either.Right<Int?>>(first)

            // when
            val second = gameService.createGame(adminId, lobbyId)

            // then
            val left = assertIs<Either.Left<GameCreationError>>(second)
            assertTrue(left.value is GameCreationError.GameAlreadyRunning)
        }

        @Test
        fun `createGame should fail if not enough players`() { // Failed
            // given - novo lobby com minUsers = 3
            val newLobby =
                lobbyService.createLobby(
                    adminId,
                    "Lobby pequeno",
                    "Faltam jogadores",
                    3,
                    5,
                    2,
                    50,
                )

            val lobbyFew = assertIs<Either.Right<Int>>(newLobby).value

            // when
            val result = gameService.createGame(adminId, lobbyFew)

            // then
            val left = assertIs<Either.Left<GameCreationError>>(result)
            assertTrue(left.value is GameCreationError.NotEnoughPlayers)
        }
    }

    @Nested
    inner class GameFlowFullTests {
        @Test
        fun `full game flow should end automatically after all rounds`() {
            println("=== Início do teste de fluxo completo ===")

            // criar game
            val createResult = gameService.createGame(adminId, lobbyId)
            val created = assertIs<Either.Right<Int?>>(createResult)
            val gameId = created.value!!

            // obterLobby
            val lobby = assertIs<Either.Right<Lobby>>(lobbyService.getLobbyById(lobbyId)).value
            val totalRounds = lobby.rounds
            var currentGame = assertIs<Either.Right<Game>>(gameService.getById(gameId)).value
            assertEquals(Game.GameStatus.RUNNING, currentGame.state)

            // simular todas as rondas
            for (roundNumber in 1..totalRounds) {
                // println("➡️  Começando ronda $roundNumber")

                // turno do admin
                val roll1 = gameService.rollDice(lobbyId, adminId)
                assertIs<Either.Right<String>>(roll1)
                val end1 = gameService.endTurn(gameId, adminId)
                assertIs<Either.Right<String>>(end1)

                // turno do segundo jogador
                val roll2 = gameService.rollDice(lobbyId, userId)
                assertIs<Either.Right<String>>(roll2)

                if (roundNumber == totalRounds) {
                    val gameResult0 = gameService.getById(gameId)
                    val gameObject0 = assertIs<Either.Right<Game>>(gameResult0).value
                    println("gameObject0 ${gameObject0.roundCounter}")
                }
                val end2 = gameService.endTurn(gameId, userId)
                assertIs<Either.Right<String>>(end2)

                //   println("✅ Ronda $roundNumber terminada")
            }
            val gameResult = gameService.getById(gameId)
            val gameObject = assertIs<Either.Right<Game>>(gameResult).value

            println("gameRounds Feitas ${gameObject.roundCounter}")
            println("totalRounds $totalRounds")

            // --- verificar estado do jogo ---
            currentGame = assertIs<Either.Right<Game>>(gameService.getById(gameId)).value
            println("Estado final do jogo: ${currentGame.state}")
            assertEquals(Game.GameStatus.CLOSED, currentGame.state, "O jogo deve estar fechado após todas as rondas")

            // --- verificar round counter ---
            assertEquals(3, currentGame.roundCounter, "O contador de rondas deve ter atingido o máximo configurado")

            // --- verificar se o lobby foi reaberto ---
            val lobbyAfter = assertIs<Either.Right<Lobby>>(lobbyService.getLobbyById(lobbyId)).value
            assertFalse(lobbyAfter.isRunning, "O lobby deve estar disponível novamente após o fim do jogo")

            println("✅ Teste completo de ciclo de jogo finalizado com sucesso!")
        }

        @Nested
        inner class InvalidTurnOrderTests {
            @Test
            fun `should fail if user tries to rollDice when it's not their turn`() {
                // --- criar jogo ---
                val createResult = gameService.createGame(adminId, lobbyId)
                val created = assertIs<Either.Right<Int?>>(createResult)
                val gameId = created.value!!

                // --- admin é o primeiro a jogar ---
                val firstRoll = gameService.rollDice(lobbyId, adminId)
                assertIs<Either.Right<String>>(firstRoll)

                // --- user tenta jogar fora da vez (não é o turno dele ainda) ---
                val invalidRoll = gameService.rollDice(lobbyId, userId)

                val leftResult = assertIs<Either.Left<GameError>>(invalidRoll)
                assertEquals(GameError.IsNotYouTurn::class, leftResult.value::class)
            }

            @Test
            fun `should fail if user tries to endTurn when it's not their turn`() {
                // --- criar jogo ---
                val createResult = gameService.createGame(adminId, lobbyId)
                val created = assertIs<Either.Right<Int?>>(createResult)
                val gameId = created.value!!

                // --- admin faz roll corretamente ---
                val firstRoll = gameService.rollDice(lobbyId, adminId)
                assertIs<Either.Right<String>>(firstRoll)

                // --- user tenta terminar turno mesmo sem ser o seu ---
                val invalidEnd = gameService.endTurn(gameId, userId)

                val leftResult = assertIs<Either.Left<GameError>>(invalidEnd)
                assertEquals(GameError.IsNotYouTurn::class, leftResult.value::class)
            }
        }
    }

    // -------------------------------------------------------------------------
    // Funções auxiliares
    // -------------------------------------------------------------------------
    companion object {
        private val jdbi =
            Jdbi
                .create(
                    PGSimpleDataSource().apply {
                        setURL("jdbc:postgresql://localhost:5432/db?user=postgres&password=postgres")
                    },
                ).configureWithAppRequirements()

        private fun createGameService(clock: Clock = Clock.System): GameService {
            val gameDomain = GameDomain()
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
            return GameService(
                transactionManager = JdbiTransactionManager(jdbi),
                gameDomain = gameDomain,
                lobbiesDomain = lobbiesDomain,
            )
        }

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
                    passwordEncoder = BCryptPasswordEncoder(),
                    tokenEncoder =
                        Sha256TokenEncoder(),
                    config =
                        UsersDomainConfig(
                            tokenSizeInBytes = 256 / 8,
                            tokenTtl = Duration.parse("30d"),
                            tokenRollingTtl = Duration.parse("30m"),
                            maxTokensPerUser = 3,
                            minUsernameLength = 3,
                            minPasswordLength = 6,
                            minAge = 18,
                            maxAge = 120,
                        ),
                )

            val inviteDomain =
                InviteDomain(
                    inviteEncoder =
                        Sha256InviteEncoder(),
                    config =
                        InviteDomainConfig(
                            expireInviteTime = Duration.parse("60m"),
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
