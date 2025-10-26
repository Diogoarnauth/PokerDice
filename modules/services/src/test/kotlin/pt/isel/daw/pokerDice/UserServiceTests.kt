package pt.isel.daw.pokerDice

import kotlinx.datetime.Clock
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import pt.isel.daw.pokerDice.domain.invite.InviteDomain
import pt.isel.daw.pokerDice.domain.users.PasswordValidationInfo
import pt.isel.daw.pokerDice.domain.users.User
import pt.isel.daw.pokerDice.domain.users.UsersDomain
import pt.isel.daw.pokerDice.repository.jdbi.JdbiTransactionManager
import pt.isel.daw.pokerDice.repository.jdbi.configureWithAppRequirements
import pt.isel.daw.pokerDice.services.DepositError
import pt.isel.daw.pokerDice.services.TokenCreationError
import pt.isel.daw.pokerDice.services.UserGetByIdError
import pt.isel.daw.pokerDice.services.UserRegisterError
import pt.isel.daw.pokerDice.services.UsersService
import pt.isel.daw.pokerDice.utils.Either
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Testes do UsersService — versão adaptada do stor.
 * Usa a BD local de desenvolvimento e domínio real.
 */
class UserServiceTests {
    @Test
    fun `can bootstrap first user`() {
        // given
        val service = createUserService()
        val username = newUsername()

        // when
        val id = service.bootstrapFirstUser(username, "Admin", 25, "StrongPass123")

        // then
        assertTrue(id > 0)
        assertTrue(service.hasAnyUser())
    }

    @Test
    fun `bootstrap should fail with invalid password`() {
        val service = createUserService()
        val username = newUsername()

        // when + then
        try {
            service.bootstrapFirstUser(username, "Admin", 25, "123")
            fail("Expected InvalidInputError.InvalidInput")
        } catch (ex: Exception) {
            // O método atual não retorna Either, apenas lança exceção/failure
            // então este catch valida o comportamento
            assertTrue(ex.message?.contains("InvalidInput") ?: true)
        }
    }

    @Test
    fun `createUser should fail with invalid data`() {
        val service = createUserService()

        val result =
            service.createUser(
                username = "",
                name = "",
                age = 5,
                password = "123",
                inviteCode = "CODE",
            )

        assertIs<Either.Left<*>>(result)
        assertTrue(
            result.value is UserRegisterError.InsecurePassword ||
                result.value is UserRegisterError.InvalidUsername ||
                result.value is UserRegisterError.InvalidName ||
                result.value is UserRegisterError.InvalidAge,
        )
    }

    @Test
    fun `createToken should fail with blank username or password`() {
        val service = createUserService()

        val res1 = service.createToken("", "abc")
        assertIs<Either.Left<*>>(res1)
        assertEquals(TokenCreationError.UserOrPasswordAreInvalid, res1.value)

        val res2 = service.createToken("abc", "")
        assertIs<Either.Left<*>>(res2)
        assertEquals(TokenCreationError.UserOrPasswordAreInvalid, res2.value)
    }

    @Test
    fun `deposit should fail when amount is zero`() {
        val service = createUserService()
        val user = dummyUser()

        val res = service.deposit(0, user)
        assertIs<Either.Left<*>>(res)
        assertEquals(DepositError.InvalidAmount, res.value)
    }

    @Test
    fun `deposit should fail when user not found`() {
        val service = createUserService()
        val user = dummyUser(id = 9999)

        val res = service.deposit(10, user)
        assertIs<Either.Left<*>>(res)
        assertEquals(DepositError.UserNotFound, res.value)
    }

    @Test
    fun `createAppInvite should create a new invite successfully`() {
        val service = createUserService()
        val userId = service.bootstrapFirstUser(newUsername(), "Admin", 25, "StrongPass123")

        val res = service.createAppInvite(userId)
        assertIs<Either.Right<*>>(res)
        assertTrue((res.value as String).isNotBlank())
    }

    @Test
    fun `getById should return the user if exists`() {
        val service = createUserService()
        val username = newUsername()
        val id = service.bootstrapFirstUser(username, "User", 25, "StrongPass123")

        val res = service.getById(id)

        // assertIs devolve o valor tipado — podes guardá-lo
        val right: Either.Right<User> = assertIs(res)
        val user: User = right.value

        assertEquals(username, user.username)
    }

    @Test
    fun `getById should fail if user not found`() {
        val service = createUserService()

        val res = service.getById(9999)
        assertIs<Either.Left<*>>(res)
        assertEquals(UserGetByIdError.UserNotFound, res.value)
    }

    // -------------------------------------------------------------------------------------
    // Funções auxiliares
    // -------------------------------------------------------------------------------------

    companion object {
        private val jdbi =
            Jdbi.create(
                PGSimpleDataSource().apply {
                    setURL("jdbc:postgresql://localhost:5432/db?user=dbuser&password=changeit")
                },
            ).configureWithAppRequirements()

        private fun createUserService(clock: Clock = Clock.System): UsersService {
            val usersDomain =
                UsersDomain(
                    passwordEncoder = org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(),
                    tokenEncoder = pt.isel.daw.pokerDice.domain.users.Sha256TokenEncoder(),
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
                    inviteEncoder = pt.isel.daw.pokerDice.domain.invite.Sha256InviteEncoder(),
                    config =
                        pt.isel.daw.pokerDice.domain.invite.InviteDomainConfig(
                            expireInviteTime = kotlin.time.Duration.parse("60m"),
                            validState = "VALID",
                            expiredState = "EXPIRED",
                            usedState = "USED",
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

        // Simples stub de utilizador para testes de depósito
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
