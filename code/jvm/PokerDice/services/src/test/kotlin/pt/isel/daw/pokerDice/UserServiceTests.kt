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

class UserServiceTests {
    private lateinit var service: usersservice
    private lateinit var username: string
    private lateinit var handle: handle
    // setup inicial com beforeeach

    @beforeeach
    fun setup() {
        service = createuserservice() // criar o serviço normalmente
        username = newusername()

        // criação da instância de jdbi
        val jdbi =
            jdbi
                .create(
                    pgsimpledatasource().apply {
                        seturl("jdbc:postgresql://localhost:5432/db?user=postgres&password=postgres")
                    },
                ).configurewithapprequirements()

        // abertura do handle
        handle = jdbi.open()

        // inicia a transação para cada teste
        handle.begin()
    }

    @aftereach
    fun teardown() {
        try {
            println("rolling back transaction")
            handle.rollback() // garante que o rollback será executado
        } catch (e: exception) {
            println("error during rollback: ${e.message}")
        } finally {
            handle.close() // fecha o handle após o rollback
        }
    }

    // --- bootstrap tests ---
    @nested
    inner class bootstraptests {
        @test
        fun `can bootstrap first user`() {
            // given
            val username = newusername()

            // when
            val id = service.bootstrapfirstuser(username, "admin", 25, "strongpass123")
            val result = assertis<either.right<int>>(id).value

            // then
            asserttrue(result > 0)
            asserttrue(service.hasanyuser())
        }

        @test
        fun `bootstrap should fail if a user already exists`() {
            // given
            val username1 = newusername()
            val username2 = newusername()

            val checkifisnotcleaning = service.getbyid(1)

            // when: cria o primeiro utilizador (deve funcionar)
            val result1 = service.bootstrapfirstuser(username1, "admin1", 30, "strongpass123")
            val id1 = assertis<either.right<int>>(result1).value
            asserttrue(id1 > 0)
            asserttrue(service.hasanyuser())

            // then: tenta criar outro e deve falhar
            val result2 = service.bootstrapfirstuser(username2, "admin2", 28, "anotherstrongpass")

            // verifica se o resultado é left e se é o erro esperado
            val error = assertis<either.left<userregistererror>>(result2).value
            asserttrue(error is userregistererror.invaliddata) // ou qualquer outro erro esperado
        }

        @test
        fun `bootstrap should fail with invalid password`() {
            val username = newusername()

            val result = service.bootstrapfirstuser(username, "admin", 25, "123")
            val error = assertis<either.left<userregistererror>>(result).value

            asserttrue(error is userregistererror.invaliddata)
        }

        // --- deposit tests ---

        @test
        fun `deposit success updates credit and returns new balance`() {
            val userid = service.bootstrapfirstuser(newusername(), "user", 25, "strongpass123")

            val result = assertis<either.right<int>>(userid).value
            // estado inicial
            val u0: user = (assertis<either.right<user>>(service.getbyid(result))).value
            assertequals(0, u0.credit)

            // deposita 50
            val res = service.deposit(50, u0)
            val r: either.right<int> = assertis(res)
            assertequals(50, r.value)

            // confirma persistência
            val u1: user = (assertis<either.right<user>>(service.getbyid(result))).value
            assertequals(50, u1.credit)
        }

        @test
        fun `deposit with zero should fail with invalidamount`() {
            val userid = service.bootstrapfirstuser(newusername(), "user", 25, "strongpass123")
            val result = assertis<either.right<int>>(userid).value
            val user: user = (assertis<either.right<user>>(service.getbyid(result))).value

            val res = service.deposit(0, user)
            val left: either.left<depositerror> = assertis(res)
            assertequals(depositerror.invalidamount, left.value)
        }

        @test
        fun `deposit with negative amount should fail with invalidamount`() {
            val userid = service.bootstrapfirstuser(newusername(), "user", 25, "strongpass123")
            val result = assertis<either.right<int>>(userid).value

            val user: user = (assertis<either.right<user>>(service.getbyid(result))).value

            val res = service.deposit(-10, user)
            val left: either.left<depositerror> = assertis(res)
            assertequals(depositerror.invalidamount, left.value)
        }

        @test
        fun `multiple deposits accumulate credit`() {
            val userid = service.bootstrapfirstuser(newusername(), "user", 25, "strongpass123")
            val result = assertis<either.right<int>>(userid).value

            val user: user = (assertis<either.right<user>>(service.getbyid(result))).value

            // 30 + 20 + 100 = 150
            val r1: either.right<int> = assertis(service.deposit(30, user))
            assertequals(30, r1.value)

            // re-obter o user após cada depósito (opcional mas claro)
            val u1: user = (assertis<either.right<user>>(service.getbyid(result))).value
            val r2: either.right<int> = assertis(service.deposit(20, u1))
            assertequals(50, r2.value)

            val u2: user = (assertis<either.right<user>>(service.getbyid(result))).value
            val r3: either.right<int> = assertis(service.deposit(100, u2))
            assertequals(150, r3.value)

            val ufinal: user = (assertis<either.right<user>>(service.getbyid(result))).value
            assertequals(150, ufinal.credit)
        }

        @test
        fun `deposit should fail when user does not exist`() {
            val ghost = dummyuser(id = 999_999)

            val res = service.deposit(10, ghost)
            val left: either.left<depositerror> = assertis(res)
            assertequals(depositerror.usernotfound, left.value)
        }

        // --- create app invite tests ---

        @test
        fun `createappinvite should succeed for existing user and return non-blank code`() {
            val userid = service.bootstrapfirstuser(newusername(), "admin", 25, "strongpass123")
            val result = assertis<either.right<int>>(userid).value

            val res = service.createappinvite(result)

            val right: either.right<string> = assertis(res)
            val code = right.value
            asserttrue(code.isnotblank(), "invite code deve ser não-vazio")
            asserttrue(code.length >= 8, "invite code deve ter um tamanho razoável")
        }

        @test
        fun `createappinvite twice should generate different codes`() {
            val userid = service.bootstrapfirstuser(newusername(), "admin", 25, "strongpass123")
            val result = assertis<either.right<int>>(userid).value

            val code1: string = (assertis<either.right<string>>(service.createappinvite(result))).value
            val code2: string = (assertis<either.right<string>>(service.createappinvite(result))).value

            assertnotequals(code1, code2, "dois convites consecutivos não devem repetir o mesmo código")
        }

        @test
        fun `createappinvite should fail when user does not exist`() {
            val ghostuserid = 999

            val res = service.createappinvite(ghostuserid)

            val left: either.left<creatingappinviteerror> = assertis(res)
            // pelo teu service, qualquer falha do repo mapeia para creatinginviteerror
            assertequals(creatingappinviteerror.usernotfound, left.value)
        }

        //  --- create user tests ---

        @test
        fun `createuser fails with insecure password`() {
            val adminid = service.bootstrapfirstuser(newusername(), "admin", 25, "strongpass123")
            val result = assertis<either.right<int>>(adminid).value

            val invite = (assertis<either.right<string>>(service.createappinvite(result))).value

            val res =
                service.createuser(
                    username = newusername(),
                    name = "user one",
                    age = 25,
                    password = "123",
                    invitecode = invite,
                )

            val left: either.left<userregistererror> = assertis(res)
            assertequals(userregistererror.insecurepassword, left.value)
        }

        @test
        fun `createuser fails with invalid username`() {
            val adminid = service.bootstrapfirstuser(newusername(), "admin", 25, "strongpass123")
            val result = assertis<either.right<int>>(adminid).value
            val invite = (assertis<either.right<string>>(service.createappinvite(result))).value

            val res =
                service.createuser(
                    username = "ab",
                    name = "user one",
                    age = 25,
                    password = "strongpass123",
                    invitecode = invite,
                )

            val left: either.left<userregistererror> = assertis(res)
            assertequals(userregistererror.invalidusername, left.value)
        }

        @test
        fun `createuser fails with invalid name`() {
            val adminid = service.bootstrapfirstuser(newusername(), "admin", 25, "strongpass123")
            val result = assertis<either.right<int>>(adminid).value
            val invite = (assertis<either.right<string>>(service.createappinvite(result))).value

            val res =
                service.createuser(
                    username = newusername(),
                    name = "",
                    age = 25,
                    password = "strongpass123",
                    invitecode = invite,
                )

            val left: either.left<userregistererror> = assertis(res)
            assertequals(userregistererror.invalidname, left.value)
        }

        @test
        fun `createuser fails with invalid age`() {
            val adminid = service.bootstrapfirstuser(newusername(), "admin", 25, "strongpass123")
            val result = assertis<either.right<int>>(adminid).value
            val invite = (assertis<either.right<string>>(service.createappinvite(result))).value

            val res =
                service.createuser(
                    username = newusername(),
                    name = "user one",
                    age = 13,
                    password = "strongpass123",
                    invitecode = invite,
                )

            val left: either.left<userregistererror> = assertis(res)
            assertequals(userregistererror.invalidage, left.value)
        }

        @test
        fun `createuser fails when invitation does not exist`() {
            val res =
                service.createuser(
                    username = newusername(),
                    name = "user one",
                    age = 25,
                    password = "strongpass123",
                    invitecode = "non_existent_invite",
                )

            val left: either.left<userregistererror> = assertis(res)
            assertequals(userregistererror.invitationdontexist, left.value)
        }

        @test
        fun `createuser fails when invitation already used`() {
            val adminid = service.bootstrapfirstuser(newusername(), "admin", 25, "strongpass123")
            val result = assertis<either.right<int>>(adminid).value

            val invite = (assertis<either.right<string>>(service.createappinvite(result))).value

            // usa o convite uma vez (sucesso)
            val uname = newusername()
            val first =
                service.createuser(
                    username = uname,
                    name = "user one",
                    age = 25,
                    password = "strongpass123",
                    invitecode = invite,
                )
            val firstright: either.right<int> = assertis(first)
            asserttrue(firstright.value > 0)

            // tenta usar o mesmo convite novamente → invitationused
            val second =
                service.createuser(
                    username = newusername(),
                    name = "user two",
                    age = 25,
                    password = "strongpass123",
                    invitecode = invite,
                )
            val left: either.left<userregistererror> = assertis(second)
            assertequals(userregistererror.invitationused, left.value)
        }

        @test
        fun `createuser fails when username already exists`() {
            val adminid = service.bootstrapfirstuser(newusername(), "admin", 25, "strongpass123")
            val result = assertis<either.right<int>>(adminid).value

            // convite 1 para o primeiro user
            val invite1 = (assertis<either.right<string>>(service.createappinvite(result))).value
            val username = newusername()

            val first =
                service.createuser(
                    username = username,
                    name = "user one",
                    age = 25,
                    password = "strongpass123",
                    invitecode = invite1,
                )
            val firstright: either.right<int> = assertis(first)
            asserttrue(firstright.value > 0)

            // convite 2 válido para a tentativa duplicada
            val invite2 = (assertis<either.right<string>>(service.createappinvite(result))).value

            // tenta criar outro com o mesmo username → useralreadyexists
            val dup =
                service.createuser(
                    username = username,
                    name = "user two",
                    age = 25,
                    password = "strongpass123",
                    invitecode = invite2,
                )
            val left: either.left<userregistererror> = assertis(dup)
            assertequals(userregistererror.useralreadyexists, left.value)
        }

        @test
        fun `createuser success stores user and marks invite as used`() {
            val adminid =
                service.bootstrapfirstuser(
                    newusername(),
                    "admin",
                    25,
                    "strongpass123",
                )
            val result = assertis<either.right<int>>(adminid).value

            val invite = (assertis<either.right<string>>(service.createappinvite(result))).value

            val username = newusername()
            val res =
                service.createuser(
                    username = username,
                    name = "user one",
                    age = 25,
                    password = "strongpass123",
                    invitecode = invite,
                )

            val right: either.right<int> = assertis(res)
            val newuserid = right.value
            asserttrue(newuserid > 0)

            // confirmar que o utilizador ficou gravado
            val user = (assertis<either.right<user>>(service.getbyid(newuserid))).value
            assertequals(username, user.username)

            // opcional: tentar reutilizar o convite deve falhar como used
            val reuse =
                service.createuser(
                    username = newusername(),
                    name = "user two",
                    age = 25,
                    password = "strongpass123",
                    invitecode = invite,
                )
            val left: either.left<userregistererror> = assertis(reuse)
            assertequals(userregistererror.invitationused, left.value)
        }

        //  --- create token tests ---

        @test
        fun `createtoken fails when username is blank`() {
            val res = service.createtoken("", "pw")
            val left: either.left<tokencreationerror> = assertis(res)
            assertequals(tokencreationerror.userorpasswordareinvalid, left.value)
        }

        @test
        fun `createtoken fails when password is blank`() {
            val res = service.createtoken("someone", "")
            val left: either.left<tokencreationerror> = assertis(res)
            assertequals(tokencreationerror.userorpasswordareinvalid, left.value)
        }

        @test
        fun `createtoken fails when user does not exist`() {
            val res = service.createtoken("ghost_user_xyz", "any")
            val left: either.left<tokencreationerror> = assertis(res)
            assertequals(tokencreationerror.userorpasswordareinvalid, left.value)
        }

        @test
        fun `createtoken fails when password is wrong`() {
            val username = newusername()
            service.bootstrapfirstuser(username, "admin", 25, "strongpass123")

            val res = service.createtoken(username, "wrong-pass")
            val left: either.left<tokencreationerror> = assertis(res)
            assertequals(tokencreationerror.userorpasswordareinvalid, left.value)
        }

        @test
        fun `createtoken success returns tokenexternalinfo and token is usable`() {
            val username = newusername()
            val pwd = "strongpass123"
            service.bootstrapfirstuser(username, "admin", 25, pwd)

            val res = service.createtoken(username, pwd)
            val right: either.right<tokenexternalinfo> = assertis(res)
            val info = right.value

            // token não-vazio
            asserttrue(info.tokenvalue.isnotblank())

            // token utilizável para obter o user
            val user = service.getuserbytoken(info.tokenvalue)
            assertnotnull(user)
            assertequals(username, user.username)
        }

        @test
        fun `createtoken respects maxtokensperuser - oldest becomes invalid`() {
            val username = newusername()
            val pwd = "strongpass123"
            service.bootstrapfirstuser(username, "admin", 25, pwd)

            val max = 3

            val tokens = mutablelistof<string>()
            repeat(max) {
                val t: either.right<tokenexternalinfo> = assertis(service.createtoken(username, pwd))
                tokens += t.value.tokenvalue
                // cada token é válido
                assertnotnull(service.getuserbytoken(tokens.last()))
            }

            // cria mais 1 (excede o limite) → o mais antigo deve ficar inválido
            val extra: either.right<tokenexternalinfo> = assertis(service.createtoken(username, pwd))
            val newest = extra.value.tokenvalue
            assertnotnull(service.getuserbytoken(newest), "o token mais recente deve ser válido")

            // o primeiro deve ter sido descartado pelo repos (política lru típica)
            val first = tokens.first()
            val stillvalidcount =
                listof(
                    service.getuserbytoken(first),
                    service.getuserbytoken(tokens[1]),
                    service.getuserbytoken(tokens[2]),
                    service.getuserbytoken(newest),
                ).count { it != null }

            // exatamente 'max' tokens devem permanecer válidos
            assertequals(max, stillvalidcount, "apenas $max tokens devem permanecer válidos")
            assertnull(service.getuserbytoken(first), "o primeiro (mais antigo) deve ter sido invalidado")
        }

        //  --- get by id tests ---

        @test
        fun `getbyid returns user when it exists`() {
            val username = newusername()
            val userid = service.bootstrapfirstuser(username, "user", 25, "strongpass123")
            val result = assertis<either.right<int>>(userid).value

            val res = service.getbyid(result)

            val right: either.right<user> = assertis(res)
            val user = right.value
            assertequals(result, user.id)
            assertequals(username, user.username)
        }

        @test
        fun `getbyid fails with usernotfound for unknown id`() {
            val res = service.getbyid(999_999)

            val left: either.left<usergetbyiderror> = assertis(res)
            assertequals(usergetbyiderror.usernotfound, left.value)
        }

        @test
        fun `getbyid with negative id also returns usernotfound (current behavior)`() {
            val res = service.getbyid(-42)

            val left: either.left<usergetbyiderror> = assertis(res)
            // o service atual não devolve invaliduserid; mapeia para usernotfound.
            assertequals(usergetbyiderror.usernotfound, left.value)
        }
    }

    // -------------------------------------------------------------------------------------
    // funções auxiliares
    // -------------------------------------------------------------------------------------

    companion object {
        private val jdbi =
            jdbi
                .create(
                    pgsimpledatasource().apply {
                        seturl("jdbc:postgresql://localhost:5432/db?user=postgres&password=postgres")
                    },
                ).configurewithapprequirements()

        private fun createuserservice(clock: clock = clock.system): usersservice {
            val usersdomain =
                usersdomain(
                    passwordencoder =
                        org.springframework.security.crypto.bcrypt
                            .bcryptpasswordencoder(),
                    tokenencoder =
                        pt.isel.daw.pokerdice.domain.users
                            .sha256tokenencoder(),
                    config =
                        pt.isel.daw.pokerdice.domain.users.usersdomainconfig(
                            tokensizeinbytes = 256 / 8,
                            tokenttl = kotlin.time.duration.parse("30d"),
                            tokenrollingttl = kotlin.time.duration.parse("30m"),
                            maxtokensperuser = 3,
                            minusernamelength = 3,
                            minpasswordlength = 6,
                            minage = 18,
                            maxage = 120,
                        ),
                )

            val invitedomain =
                pt.isel.daw.pokerdice.domain.invite.invitedomain(
                    inviteencoder =
                        pt.isel.daw.pokerdice.domain.invite
                            .sha256inviteencoder(),
                    config =
                        pt.isel.daw.pokerdice.domain.invite.invitedomainconfig(
                            expireinvitetime = kotlin.time.duration.parse("60m"),
                            validstate = "pending",
                            expiredstate = "expired",
                            usedstate = "used",
                            declinedstate = "declined",
                        ),
                )

            return usersservice(
                transactionmanager = jdbitransactionmanager(jdbi),
                userdomain = usersdomain,
                invitedomain = invitedomain,
                clock = clock,
            )
        }

        private fun newusername() = "user-${random.nextint(1_000_000)}"

        private fun dummyuser(id: int = 1) =
            user(
                id = id,
                username = "dummy",
                passwordvalidation = passwordvalidationinfo("x"),
                name = "dummy",
                age = 30,
                credit = 0,
                wincounter = 0,
                lobbyid = null,
            )
    }
}
