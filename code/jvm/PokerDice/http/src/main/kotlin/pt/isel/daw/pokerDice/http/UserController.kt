package pt.isel.daw.pokerDice.http

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import pt.isel.daw.pokerDice.domain.Topic
import pt.isel.daw.pokerDice.domain.users.AuthenticatedUser
import pt.isel.daw.pokerDice.domain.users.User
import pt.isel.daw.pokerDice.events.SseEmitterBasedEventEmitter
import pt.isel.daw.pokerDice.http.model.Problem
import pt.isel.daw.pokerDice.http.model.inviteModel.InviteAppOutputModel
import pt.isel.daw.pokerDice.http.model.userModel.BootstrapRegisterInputModel
import pt.isel.daw.pokerDice.http.model.userModel.DepositInputModel
import pt.isel.daw.pokerDice.http.model.userModel.UserCreateInputModel
import pt.isel.daw.pokerDice.http.model.userModel.UserCreateTokenInputModel
import pt.isel.daw.pokerDice.http.model.userModel.UserGetByIdOutputModel
import pt.isel.daw.pokerDice.http.model.userModel.UserTokenCreateOutputModel
import pt.isel.daw.pokerDice.services.CreatingAppInviteError
import pt.isel.daw.pokerDice.services.DepositError
import pt.isel.daw.pokerDice.services.PokerDiceEventService
import pt.isel.daw.pokerDice.services.TokenCreationError
import pt.isel.daw.pokerDice.services.UserGetByIdError
import pt.isel.daw.pokerDice.services.UserRegisterError
import pt.isel.daw.pokerDice.services.UsersService
import pt.isel.daw.pokerDice.utils.Failure
import pt.isel.daw.pokerDice.utils.Success
import java.time.Duration
import java.util.concurrent.TimeUnit

@RestController
class UserController(
    private val userService: UsersService,
    private val eventService: PokerDiceEventService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping(Uris.Users.BOOTSTRAP)
    fun bootstrapAdmin(
        @RequestBody input: BootstrapRegisterInputModel,
    ): ResponseEntity<*> {
        val res =
            userService.bootstrapFirstUser(
                input.username,
                input.name,
                input.age,
                input.password,
            )

        return when (res) {
            is Success -> {
                val id = res.value
                ResponseEntity
                    .status(201)
                    .body(
                        mapOf(
                            "id" to id,
                            "location" to Uris.Users.byId(id).toASCIIString(),
                        ),
                    )
            }

            is Failure -> {
                when (res.value) {
                    UserRegisterError.InvalidData -> {
                        Problem.response(400, Problem.InvalidData)
                    }

                    UserRegisterError.AdminAlreadyExists -> {
                        Problem.response(403, Problem.AdminAlreadyExists)
                    }

                    else -> {
                        TODO()
                    }
                }
            }
        }
    }

    @PostMapping(Uris.Users.DEPOSIT)
    fun deposit(
        @RequestBody input: DepositInputModel,
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
    ): ResponseEntity<*> {
        val res = userService.deposit(input.value, authenticatedUser.user)

        return when (res) {
            is Success -> {
                ResponseEntity.ok(mapOf("message" to "Deposit successful", "newBalance" to res.value))
            }

            is Failure -> {
                when (res.value) {
                    DepositError.InvalidAmount -> {
                        ResponseEntity
                            .badRequest()
                            .body(mapOf("error" to Problem.badDeposit))
                    }

                    DepositError.UserNotFound -> {
                        ResponseEntity
                            .status(404)
                            .body(mapOf("error" to Problem.userNotFound))
                    }
                }
            }
        }
    }

    @PostMapping(Uris.Users.CREATE)
    fun create(
        @RequestBody input: UserCreateInputModel,
    ): ResponseEntity<*> {
        val res = userService.createUser(input.username, input.name, input.age, input.password, input.inviteCode)
        return when (res) {
            is Success -> {
                ResponseEntity
                    .status(201)
                    .body(mapOf("id" to res.value, "location" to Uris.Users.byId(res.value).toASCIIString()))
            }

            is Failure -> {
                when (res.value) {
                    is UserRegisterError.InsecurePassword -> {
                        Problem.response(400, Problem.insecurePassword)
                    }

                    is UserRegisterError.InvalidUsername -> {
                        Problem.response(400, Problem.InvalidData)
                    }

                    is UserRegisterError.InvalidAge -> {
                        Problem.response(400, Problem.InvalidData)
                    }

                    is UserRegisterError.InvalidName -> {
                        Problem.response(400, Problem.InvalidData)
                    }

                    is UserRegisterError.UserAlreadyExists -> {
                        Problem.response(409, Problem.userAlreadyExists)
                    }

                    is UserRegisterError.InvitationDontExist -> {
                        Problem.response(404, Problem.invitationDontExist)
                    }

                    is UserRegisterError.InvitationUsed -> {
                        Problem.response(409, Problem.invitationUsed)
                    }

                    is UserRegisterError.InvitationExpired -> {
                        Problem.response(410, Problem.invitationExpired)
                    }

                    else -> {
                        TODO("else -> Problem.response(400, Problem.InvalidData) ?????")
                    }
                }
            }
        }
    }

    @PostMapping(Uris.Users.TOKEN)
    fun token(
        @RequestBody input: UserCreateTokenInputModel,
    ): ResponseEntity<*> {
        val res = userService.createToken(input.username, input.password)
        logger.info("BOLACHAS DE CHOCOLATE")

        return when (res) {
            is Success -> {
                val token = res.value.tokenValue

                val cookie =
                    ResponseCookie
                        .from("token", token)
                        .path("/")
                        .httpOnly(true)
                        .secure(false) // False para localhost (True só com HTTPS)
                        .sameSite("Lax") // Lax é mais flexivel do que Strict
                        .maxAge(Duration.ofHours(24))
                        .build()

                /*val cookie = Cookie("token", token)
                cookie.path = "/"
                cookie.maxAge = 24 * 60 * 60
                cookie.secure = false
                 val cookieHeader =
                    "token=${cookie.value}; Path=${cookie.path}; Max-Age=${cookie.maxAge}; SameSite=Strict; Secure"
                    // hardcoded
                 */
                ResponseEntity
                    .status(200)
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(UserTokenCreateOutputModel(res.value.tokenValue))
            }

            is Failure -> {
                when (res.value) {
                    TokenCreationError.UserOrPasswordAreInvalid -> {
                        Problem.response(
                            401,
                            Problem.userOrPasswordAreInvalid,
                        )
                    }

                    TokenCreationError.TokenLimitReached -> {
                        Problem.response(401, Problem.userNotAuthorized)
                    }

                    TokenCreationError.UserNotFound -> {
                        Problem.response(401, Problem.userNotFound)
                    }
                }
            }
        }
    }

    @PostMapping(Uris.Users.LOGOUT)
    fun logout(userAuthenticatedUser: AuthenticatedUser): ResponseEntity<*> {
        userService.deleteToken(userAuthenticatedUser.user.id)
        val listener = eventService.getListener(userAuthenticatedUser.user.id)
        eventService.logout(listener!!)
        /*val cookie = Cookie("token", "")
        cookie.path = "/"
        cookie.maxAge = 0 // TODO(N É 0 ?)
        val cookieHeader = "token=${cookie.value}; Path=${cookie.path}; Max-Age=${cookie.maxAge}"*/
        val cookie =
            ResponseCookie
                .from("token", "")
                .path("/")
                .httpOnly(true)
                .maxAge(0)
                .build()

        return ResponseEntity
            .status(204)
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .build<Unit>()
    }

    @GetMapping(Uris.Users.GET_BY_ID)
    fun getById(
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val res = userService.getById(id)

        return when (res) {
            is Success -> {
                val user: User = res.value

                ResponseEntity.ok(
                    UserGetByIdOutputModel(
                        username = user.username,
                        name = user.name,
                        age = user.age,
                        lobby = user.lobbyId,
                        credits = user.credit,
                        winCounts = user.winCounter,
                    ),
                )
            }

            is Failure -> {
                ResponseEntity
                    .status(
                        when (res.value) {
                            UserGetByIdError.UserNotFound -> 404
                            UserGetByIdError.InvalidUserId -> 400
                        },
                    ).body(
                        mapOf(
                            "error" to
                                when (res.value) {
                                    UserGetByIdError.UserNotFound -> {
                                        Problem.userNotFound
                                    }

                                    else -> {
                                        TODO()
                                    }
                                },
                        ),
                    )
            }
        }
    }

    @GetMapping(Uris.Users.CHECK_ADMIN)
    fun checkAdmin(): ResponseEntity<Map<String, Boolean>> {
        val firstUser = userService.checkAdmin()
        return ResponseEntity.ok(mapOf("firstUser" to firstUser))
    }

    @GetMapping(Uris.Users.LISTEN)
    fun listen(
        @RequestParam topic: String,
        user: AuthenticatedUser,
    ): ResponseEntity<SseEmitter> {
        logger.info("Received request to listen on topic: $topic from user: ${user.user.username}")

        // converter string -> Topic sealed class
        val resolvedTopic = resolveTopic(topic)

        val sseEmitter = SseEmitter(TimeUnit.HOURS.toMillis(1))

        eventService.addEventEmitter(
            userId = user.user.id,
            topic = resolvedTopic,
            listener = SseEmitterBasedEventEmitter(sseEmitter),
        )

        return ResponseEntity
            .status(200)
            .header("Content-Type", "text/event-stream; charset=utf-8")
            .header("Connection", "keep-alive")
            .header("X-Accel-Buffering", "no")
            .body(sseEmitter)
    }

    private fun resolveTopic(raw: String): Topic =
        when {
            raw == "lobbies" -> Topic.Lobbies

            /* raw.startsWith("lobby:") -> {
                 val id = raw.removePrefix("lobby:").toInt()
                 Topic.Lobby(id)
             }

             raw.startsWith("game:") -> {
                 val id = raw.removePrefix("game:").toInt()
                 Topic.Game(id)
             }*/

            // raw == "profile" -> Topic.Profile
            // raw == "home" -> Topic.Home

            else -> Topic.Home // fallback seguro
        }

    @PostMapping(Uris.Users.INVITE)
    fun appInvite(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
    ): ResponseEntity<*> {
        val res = userService.createAppInvite(authenticatedUser.user.id)
        return when (res) {
            is Success -> {
                ResponseEntity
                    .status(201)
                    .body(InviteAppOutputModel(res.value))
            }

            is Failure -> {
                when (res.value) {
                    CreatingAppInviteError.UserNotFound -> {
                        Problem.response(404, Problem.userNotFound)
                    }

                    CreatingAppInviteError.CreatingInviteError -> {
                        Problem.response(500, Problem.inviteCreationError)
                    }
                }
            }
        }
    }

    @GetMapping(Uris.Users.GETOBJPLAYERSONLOBBY)
    fun getObjPlayersOnLobby(
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val res = userService.getObjPlayersOnLobby(id)

        return when (res) {
            is Success -> {
                val users =
                    res.value.map { user ->
                        mapOf(
                            "id" to user.id,
                            "username" to user.username,
                            "name" to user.name,
                            "age" to user.age,
                            "credit" to user.credit,
                            "winCounter" to user.winCounter,
                            "lobbyId" to user.lobbyId,
                        )
                    }

                ResponseEntity.ok(
                    mapOf(
                        "lobbyId" to id,
                        "count" to users.size,
                        "players" to users,
                    ),
                )
            }

            is Failure -> Problem.response(404, Problem.lobbyNotFound)
        }
    }

    @GetMapping(Uris.Users.GETPLAYERSONLOBBY)
    fun getPlayersOnLobby(
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val res = userService.getPlayersInLobby(id)

        return when (res) {
            is Success -> {
                ResponseEntity.ok(
                    mapOf(
                        "lobbyId" to id,
                        "count" to res.value,
                    ),
                )
            }

            is Failure -> {
                Problem.response(404, Problem.lobbyNotFound)
            }
        }
    }

    @GetMapping(Uris.Users.GETME)
    fun getUserByToken(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
    ): ResponseEntity<*> {
        val user = authenticatedUser.user

        return ResponseEntity.ok(
            mapOf(
                "id" to user.id,
                "username" to user.username,
                "name" to user.name,
                "age" to user.age,
                "credit" to user.credit,
                "winCounter" to user.winCounter,
                "lobbyId" to user.lobbyId,
            ),
        )
    }
}
