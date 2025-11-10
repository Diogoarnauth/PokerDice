
package pt.isel.daw.pokerDice

import kotlinx.datetime.Clock
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.postgresql.ds.PGSimpleDataSource
import pt.isel.daw.pokerDice.domain.users.PasswordValidationInfo
import pt.isel.daw.pokerDice.domain.users.User
import pt.isel.daw.pokerDice.domain.users.UsersDomain
import pt.isel.daw.pokerDice.repository.jdbi.JdbiTransactionManager
import pt.isel.daw.pokerDice.repository.jdbi.configureWithAppRequirements
import pt.isel.daw.pokerDice.services.CreatingAppInviteError
import pt.isel.daw.pokerDice.services.DepositError
import pt.isel.daw.pokerDice.services.TokenCreationError
import pt.isel.daw.pokerDice.services.TokenExternalInfo
import pt.isel.daw.pokerDice.services.UserGetByIdError
import pt.isel.daw.pokerDice.services.UserRegisterError
import pt.isel.daw.pokerDice.services.UsersService
import pt.isel.daw.pokerDice.utils.Either
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Testes do UsersService — versão adaptada do stor.
 * Usa a BD local de desenvolvimento e domínio real.
 */

class UserServiceTests {
    private lateinit var service: UsersService
    private lateinit var username: String
    private lateinit var handle: Handle
    // Setup inicial com BeforeEach

    @BeforeEach
    fun setup() {
        service = createUserService() // Criar o serviço normalmente
        username = newUsername()

        // Criação da instância de Jdbi
        val jdbi =
            Jdbi
                .create(
                    PGSimpleDataSource().apply {
                        setURL("jdbc:postgresql://localhost:5432/db?user=postgres&password=postgres")
                    },
                ).configureWithAppRequirements()

        // Abertura do handle
        handle = jdbi.open()

        // Inicia a transação para cada teste
        handle.begin()
    }

    @AfterEach
    fun tearDown() {
        try {
            println("Rolling back transaction")
            handle.rollback() // Garante que o rollback será executado
        } catch (e: Exception) {
            println("Error during rollback: ${e.message}")
        } finally {
            handle.close() // Fecha o handle após o rollback
        }
    }

    // --- BOOTSTRAP TESTS ---
    @Nested
    inner class BootstrapTests {
        @Test
        fun `can bootstrap first user`() {
            // given
            val username = newUsername()

            // when
            val id = service.bootstrapFirstUser(username, "Admin", 25, "StrongPass123")
            val result = assertIs<Either.Right<Int>>(id).value

            // then
            assertTrue(result > 0)
            assertTrue(service.hasAnyUser())
        }

        @Test
        fun `bootstrap should fail if a user already exists`() {
            // given
            val username1 = newUsername()
            val username2 = newUsername()

            val checkIfIsNotCleaning = service.getById(1)

            // when: cria o primeiro utilizador (deve funcionar)
            val result1 = service.bootstrapFirstUser(username1, "Admin1", 30, "StrongPass123")
            val id1 = assertIs<Either.Right<Int>>(result1).value
            assertTrue(id1 > 0)
            assertTrue(service.hasAnyUser())

            // then: tenta criar outro e deve falhar
            val result2 = service.bootstrapFirstUser(username2, "Admin2", 28, "AnotherStrongPass")

            // Verifica se o resultado é Left e se é o erro esperado
            val error = assertIs<Either.Left<UserRegisterError>>(result2).value
            assertTrue(error is UserRegisterError.InvalidData) // ou qualquer outro erro esperado
        }

        @Test
        fun `bootstrap should fail with invalid password`() {
            val username = newUsername()

            val result = service.bootstrapFirstUser(username, "Admin", 25, "123")
            val error = assertIs<Either.Left<UserRegisterError>>(result).value

            assertTrue(error is UserRegisterError.InvalidData)
        }

        // --- DEPOSIT TESTS ---

        @Test
        fun `deposit success updates credit and returns new balance`() {
            val userId = service.bootstrapFirstUser(newUsername(), "User", 25, "StrongPass123")

            val result = assertIs<Either.Right<Int>>(userId).value
            // estado inicial
            val u0: User = (assertIs<Either.Right<User>>(service.getById(result))).value
            assertEquals(0, u0.credit)

            // deposita 50
            val res = service.deposit(50, u0)
            val r: Either.Right<Int> = assertIs(res)
            assertEquals(50, r.value)

            // confirma persistência
            val u1: User = (assertIs<Either.Right<User>>(service.getById(result))).value
            assertEquals(50, u1.credit)
        }

        @Test
        fun `deposit with zero should fail with InvalidAmount`() {
            val userId = service.bootstrapFirstUser(newUsername(), "User", 25, "StrongPass123")
            val result = assertIs<Either.Right<Int>>(userId).value
            val user: User = (assertIs<Either.Right<User>>(service.getById(result))).value

            val res = service.deposit(0, user)
            val left: Either.Left<DepositError> = assertIs(res)
            assertEquals(DepositError.InvalidAmount, left.value)
        }

        @Test
        fun `deposit with negative amount should fail with InvalidAmount`() {
            val userId = service.bootstrapFirstUser(newUsername(), "User", 25, "StrongPass123")
            val result = assertIs<Either.Right<Int>>(userId).value

            val user: User = (assertIs<Either.Right<User>>(service.getById(result))).value

            val res = service.deposit(-10, user)
            val left: Either.Left<DepositError> = assertIs(res)
            assertEquals(DepositError.InvalidAmount, left.value)
        }

        @Test
        fun `multiple deposits accumulate credit`() {
            val userId = service.bootstrapFirstUser(newUsername(), "User", 25, "StrongPass123")
            val result = assertIs<Either.Right<Int>>(userId).value

            val user: User = (assertIs<Either.Right<User>>(service.getById(result))).value

            // 30 + 20 + 100 = 150
            val r1: Either.Right<Int> = assertIs(service.deposit(30, user))
            assertEquals(30, r1.value)

            // re-obter o user após cada depósito (opcional mas claro)
            val u1: User = (assertIs<Either.Right<User>>(service.getById(result))).value
            val r2: Either.Right<Int> = assertIs(service.deposit(20, u1))
            assertEquals(50, r2.value)

            val u2: User = (assertIs<Either.Right<User>>(service.getById(result))).value
            val r3: Either.Right<Int> = assertIs(service.deposit(100, u2))
            assertEquals(150, r3.value)

            val uFinal: User = (assertIs<Either.Right<User>>(service.getById(result))).value
            assertEquals(150, uFinal.credit)
        }

        @Test
        fun `deposit should fail when user does not exist`() {
            val ghost = dummyUser(id = 999_999)

            val res = service.deposit(10, ghost)
            val left: Either.Left<DepositError> = assertIs(res)
            assertEquals(DepositError.UserNotFound, left.value)
        }

        // --- CREATE APP INVITE TESTS ---

        @Test
        fun `createAppInvite should succeed for existing user and return non-blank code`() {
            val userId = service.bootstrapFirstUser(newUsername(), "Admin", 25, "StrongPass123")
            val result = assertIs<Either.Right<Int>>(userId).value

            val res = service.createAppInvite(result)

            val right: Either.Right<String> = assertIs(res)
            val code = right.value
            assertTrue(code.isNotBlank(), "invite code deve ser não-vazio")
            assertTrue(code.length >= 8, "invite code deve ter um tamanho razoável")
        }

        @Test
        fun `createAppInvite twice should generate different codes`() {
            val userId = service.bootstrapFirstUser(newUsername(), "Admin", 25, "StrongPass123")
            val result = assertIs<Either.Right<Int>>(userId).value

            val code1: String = (assertIs<Either.Right<String>>(service.createAppInvite(result))).value
            val code2: String = (assertIs<Either.Right<String>>(service.createAppInvite(result))).value

            assertNotEquals(code1, code2, "dois convites consecutivos não devem repetir o mesmo código")
        }

        @Test
        fun `createAppInvite should fail when user does not exist`() {
            val ghostUserId = 999

            val res = service.createAppInvite(ghostUserId)

            val left: Either.Left<CreatingAppInviteError> = assertIs(res)
            // pelo teu service, qualquer falha do repo mapeia para CreatingInviteError
            assertEquals(CreatingAppInviteError.UserNotFound, left.value)
        }

        //  --- CREATE USER TESTS ---

        @Test
        fun `createUser fails with insecure password`() {
            val adminId = service.bootstrapFirstUser(newUsername(), "Admin", 25, "StrongPass123")
            val result = assertIs<Either.Right<Int>>(adminId).value

            val invite = (assertIs<Either.Right<String>>(service.createAppInvite(result))).value

            val res =
                service.createUser(
                    username = newUsername(),
                    name = "User One",
                    age = 25,
                    password = "123",
                    inviteCode = invite,
                )

            val left: Either.Left<UserRegisterError> = assertIs(res)
            assertEquals(UserRegisterError.InsecurePassword, left.value)
        }

        @Test
        fun `createUser fails with invalid username`() {
            val adminId = service.bootstrapFirstUser(newUsername(), "Admin", 25, "StrongPass123")
            val result = assertIs<Either.Right<Int>>(adminId).value
            val invite = (assertIs<Either.Right<String>>(service.createAppInvite(result))).value

            val res =
                service.createUser(
                    username = "ab",
                    name = "User One",
                    age = 25,
                    password = "StrongPass123",
                    inviteCode = invite,
                )

            val left: Either.Left<UserRegisterError> = assertIs(res)
            assertEquals(UserRegisterError.InvalidUsername, left.value)
        }

        @Test
        fun `createUser fails with invalid name`() {
            val adminId = service.bootstrapFirstUser(newUsername(), "Admin", 25, "StrongPass123")
            val result = assertIs<Either.Right<Int>>(adminId).value
            val invite = (assertIs<Either.Right<String>>(service.createAppInvite(result))).value

            val res =
                service.createUser(
                    username = newUsername(),
                    name = "",
                    age = 25,
                    password = "StrongPass123",
                    inviteCode = invite,
                )

            val left: Either.Left<UserRegisterError> = assertIs(res)
            assertEquals(UserRegisterError.InvalidName, left.value)
        }

        @Test
        fun `createUser fails with invalid age`() {
            val adminId = service.bootstrapFirstUser(newUsername(), "Admin", 25, "StrongPass123")
            val result = assertIs<Either.Right<Int>>(adminId).value
            val invite = (assertIs<Either.Right<String>>(service.createAppInvite(result))).value

            val res =
                service.createUser(
                    username = newUsername(),
                    name = "User One",
                    age = 13,
                    password = "StrongPass123",
                    inviteCode = invite,
                )

            val left: Either.Left<UserRegisterError> = assertIs(res)
            assertEquals(UserRegisterError.InvalidAge, left.value)
        }

        @Test
        fun `createUser fails when invitation does not exist`() {
            val res =
                service.createUser(
                    username = newUsername(),
                    name = "User One",
                    age = 25,
                    password = "StrongPass123",
                    inviteCode = "NON_EXISTENT_INVITE",
                )

            val left: Either.Left<UserRegisterError> = assertIs(res)
            assertEquals(UserRegisterError.InvitationDontExist, left.value)
        }

        @Test
        fun `createUser fails when invitation already used`() {
            val adminId = service.bootstrapFirstUser(newUsername(), "Admin", 25, "StrongPass123")
            val result = assertIs<Either.Right<Int>>(adminId).value

            val invite = (assertIs<Either.Right<String>>(service.createAppInvite(result))).value

            // usa o convite uma vez (sucesso)
            val uname = newUsername()
            val first =
                service.createUser(
                    username = uname,
                    name = "User One",
                    age = 25,
                    password = "StrongPass123",
                    inviteCode = invite,
                )
            val firstRight: Either.Right<Int> = assertIs(first)
            assertTrue(firstRight.value > 0)

            // tenta usar o MESMO convite novamente → InvitationUsed
            val second =
                service.createUser(
                    username = newUsername(),
                    name = "User Two",
                    age = 25,
                    password = "StrongPass123",
                    inviteCode = invite,
                )
            val left: Either.Left<UserRegisterError> = assertIs(second)
            assertEquals(UserRegisterError.InvitationUsed, left.value)
        }

        @Test
        fun `createUser fails when username already exists`() {
            val adminId = service.bootstrapFirstUser(newUsername(), "Admin", 25, "StrongPass123")
            val result = assertIs<Either.Right<Int>>(adminId).value

            // convite 1 para o primeiro user
            val invite1 = (assertIs<Either.Right<String>>(service.createAppInvite(result))).value
            val username = newUsername()

            val first =
                service.createUser(
                    username = username,
                    name = "User One",
                    age = 25,
                    password = "StrongPass123",
                    inviteCode = invite1,
                )
            val firstRight: Either.Right<Int> = assertIs(first)
            assertTrue(firstRight.value > 0)

            // convite 2 válido para a tentativa duplicada
            val invite2 = (assertIs<Either.Right<String>>(service.createAppInvite(result))).value

            // tenta criar outro com o MESMO username → UserAlreadyExists
            val dup =
                service.createUser(
                    username = username,
                    name = "User Two",
                    age = 25,
                    password = "StrongPass123",
                    inviteCode = invite2,
                )
            val left: Either.Left<UserRegisterError> = assertIs(dup)
            assertEquals(UserRegisterError.UserAlreadyExists, left.value)
        }

        @Test
        fun `createUser success stores user and marks invite as used`() {
            val adminId =
                service.bootstrapFirstUser(
                    newUsername(),
                    "Admin",
                    25,
                    "StrongPass123",
                )
            val result = assertIs<Either.Right<Int>>(adminId).value

            val invite = (assertIs<Either.Right<String>>(service.createAppInvite(result))).value

            val username = newUsername()
            val res =
                service.createUser(
                    username = username,
                    name = "User One",
                    age = 25,
                    password = "StrongPass123",
                    inviteCode = invite,
                )

            val right: Either.Right<Int> = assertIs(res)
            val newUserId = right.value
            assertTrue(newUserId > 0)

            // confirmar que o utilizador ficou gravado
            val user = (assertIs<Either.Right<User>>(service.getById(newUserId))).value
            assertEquals(username, user.username)

            // opcional: tentar reutilizar o convite deve falhar como USED
            val reuse =
                service.createUser(
                    username = newUsername(),
                    name = "User Two",
                    age = 25,
                    password = "StrongPass123",
                    inviteCode = invite,
                )
            val left: Either.Left<UserRegisterError> = assertIs(reuse)
            assertEquals(UserRegisterError.InvitationUsed, left.value)
        }

        //  --- CREATE TOKEN TESTS ---

        @Test
        fun `createToken fails when username is blank`() {
            val res = service.createToken("", "pw")
            val left: Either.Left<TokenCreationError> = assertIs(res)
            assertEquals(TokenCreationError.UserOrPasswordAreInvalid, left.value)
        }

        @Test
        fun `createToken fails when password is blank`() {
            val res = service.createToken("someone", "")
            val left: Either.Left<TokenCreationError> = assertIs(res)
            assertEquals(TokenCreationError.UserOrPasswordAreInvalid, left.value)
        }

        @Test
        fun `createToken fails when user does not exist`() {
            val res = service.createToken("ghost_user_xyz", "any")
            val left: Either.Left<TokenCreationError> = assertIs(res)
            assertEquals(TokenCreationError.UserOrPasswordAreInvalid, left.value)
        }

        @Test
        fun `createToken fails when password is wrong`() {
            val username = newUsername()
            service.bootstrapFirstUser(username, "Admin", 25, "StrongPass123")

            val res = service.createToken(username, "wrong-pass")
            val left: Either.Left<TokenCreationError> = assertIs(res)
            assertEquals(TokenCreationError.UserOrPasswordAreInvalid, left.value)
        }

        @Test
        fun `createToken success returns TokenExternalInfo and token is usable`() {
            val username = newUsername()
            val pwd = "StrongPass123"
            service.bootstrapFirstUser(username, "Admin", 25, pwd)

            val res = service.createToken(username, pwd)
            val right: Either.Right<TokenExternalInfo> = assertIs(res)
            val info = right.value

            // token não-vazio
            assertTrue(info.tokenValue.isNotBlank())

            // token utilizável para obter o user
            val user = service.getUserByToken(info.tokenValue)
            assertNotNull(user)
            assertEquals(username, user.username)
        }

        @Test
        fun `createToken respects maxTokensPerUser - oldest becomes invalid`() {
            val username = newUsername()
            val pwd = "StrongPass123"
            service.bootstrapFirstUser(username, "Admin", 25, pwd)

            val max = 3

            val tokens = mutableListOf<String>()
            repeat(max) {
                val t: Either.Right<TokenExternalInfo> = assertIs(service.createToken(username, pwd))
                tokens += t.value.tokenValue
                // cada token é válido
                assertNotNull(service.getUserByToken(tokens.last()))
            }

            // cria mais 1 (excede o limite) → o mais antigo deve ficar inválido
            val extra: Either.Right<TokenExternalInfo> = assertIs(service.createToken(username, pwd))
            val newest = extra.value.tokenValue
            assertNotNull(service.getUserByToken(newest), "o token mais recente deve ser válido")

            // o primeiro deve ter sido descartado pelo repos (política LRU típica)
            val first = tokens.first()
            val stillValidCount =
                listOf(
                    service.getUserByToken(first),
                    service.getUserByToken(tokens[1]),
                    service.getUserByToken(tokens[2]),
                    service.getUserByToken(newest),
                ).count { it != null }

            // exatamente 'max' tokens devem permanecer válidos
            assertEquals(max, stillValidCount, "apenas $max tokens devem permanecer válidos")
            assertNull(service.getUserByToken(first), "o primeiro (mais antigo) deve ter sido invalidado")
        }

        //  --- GET BY ID TESTS ---

        @Test
        fun `getById returns user when it exists`() {
            val username = newUsername()
            val userId = service.bootstrapFirstUser(username, "User", 25, "StrongPass123")
            val result = assertIs<Either.Right<Int>>(userId).value

            val res = service.getById(result)

            val right: Either.Right<User> = assertIs(res)
            val user = right.value
            assertEquals(result, user.id)
            assertEquals(username, user.username)
        }

        @Test
        fun `getById fails with UserNotFound for unknown id`() {
            val res = service.getById(999_999)

            val left: Either.Left<UserGetByIdError> = assertIs(res)
            assertEquals(UserGetByIdError.UserNotFound, left.value)
        }

        @Test
        fun `getById with negative id also returns UserNotFound (current behavior)`() {
            val res = service.getById(-42)

            val left: Either.Left<UserGetByIdError> = assertIs(res)
            // O service atual não devolve InvalidUserId; mapeia para UserNotFound.
            assertEquals(UserGetByIdError.UserNotFound, left.value)
        }
    }

    // -------------------------------------------------------------------------------------
    // Funções auxiliares
    // -------------------------------------------------------------------------------------

    companion object {
        private val jdbi =
            Jdbi
                .create(
                    PGSimpleDataSource().apply {
                        setURL("jdbc:postgresql://localhost:5432/db?user=postgres&password=postgres")
                    },
                ).configureWithAppRequirements()

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

        private fun dummyUser(id: Int = 1) =
            User(
                id = id,
                username = "dummy",
                passwordValidation = PasswordValidationInfo("x"),
                name = "Dummy",
                age = 30,
                credit = 0,
                winCounter = 0,
                lobbyId = null,
            )
    }
}
