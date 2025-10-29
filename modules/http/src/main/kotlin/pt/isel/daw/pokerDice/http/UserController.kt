package pt.isel.daw.pokerDice.http

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pt.isel.daw.pokerDice.domain.users.AuthenticatedUser
import pt.isel.daw.pokerDice.domain.users.User
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
import pt.isel.daw.pokerDice.services.TokenCreationError
import pt.isel.daw.pokerDice.services.UserGetByIdError
import pt.isel.daw.pokerDice.services.UserRegisterError
import pt.isel.daw.pokerDice.services.UsersService
import pt.isel.daw.pokerDice.utils.Failure
import pt.isel.daw.pokerDice.utils.Success

@RestController
class UserController(
    private val userService: UsersService,
) {
    @PostMapping("/bootstrap")
    fun bootstrapAdmin(
        @RequestBody input: BootstrapRegisterInputModel,
    ): ResponseEntity<*> {
        println("entrou")
        // lógica passou para o services
        // if (userService.hasAnyUser()) {
        //      return ResponseEntity
        //          .status(403)
        //          .body(mapOf("error" to Problem.userAlreadyExists))
        //  }

        val id = userService.bootstrapFirstUser(input.username, input.name, input.age, input.password)
        return ResponseEntity.ok(mapOf("id" to id))
    }

    @PostMapping("/deposit")
    fun deposit(
        @RequestBody input: DepositInputModel,
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
    ): ResponseEntity<*> {
        val res = userService.deposit(input.value, authenticatedUser.user)

        return when (res) {
            is Success ->
                ResponseEntity.ok(mapOf("message" to "Deposit successful", "newBalance" to res.value))

            is Failure ->
                when (res.value) {
                    DepositError.InvalidAmount ->
                        ResponseEntity
                            .badRequest()
                            .body(mapOf("error" to Problem.badDeposit))
                    DepositError.UserNotFound ->
                        ResponseEntity
                            .status(404)
                            .body(mapOf("error" to Problem.userNotFound))
                }
        }
    }

    @PostMapping(UserUris.Users.CREATE)
    fun create(
        @RequestBody input: UserCreateInputModel,
    ): ResponseEntity<*> {
        val res = userService.createUser(input.username, input.name, input.age, input.password, input.inviteCode)
        return when (res) {
            is Success ->
                ResponseEntity
                    .status(201)
                    .body(mapOf("id" to res.value, "location" to UserUris.Users.byId(res.value).toASCIIString()))

            is Failure ->
                ResponseEntity.status(400).body(
                    mapOf(
                        "error" to
                            when (res.value) {
                                UserRegisterError.InsecurePassword -> Problem.insecurePassword
                                UserRegisterError.UserAlreadyExists -> Problem.userAlreadyExists
                                UserRegisterError.InvalidAge -> Problem.todo
                                UserRegisterError.InvalidName -> Problem.todo
                                UserRegisterError.InvalidUsername -> Problem.todo
                                UserRegisterError.InvitationDontExist -> Problem.invitationDontExist
                                UserRegisterError.InvitationUsed -> Problem.invitationUsed
                                UserRegisterError.InvitationExpired -> Problem.invitationExpired
                                UserRegisterError.AdminAlreadyExists -> Problem.AdminAlreadyExists
                                UserRegisterError.InvalidData -> Problem.InvalidData
                            },
                    ),
                )
        }
    }

    @PostMapping(UserUris.Users.TOKEN)
    fun token(
        @RequestBody input: UserCreateTokenInputModel,
    ): ResponseEntity<*> {
        val res = userService.createToken(input.username, input.password)
        return when (res) {
            is Success ->
                ResponseEntity.ok(UserTokenCreateOutputModel(res.value.tokenValue))

            is Failure ->
                ResponseEntity
                    .status(
                        when (res.value) {
                            TokenCreationError.UserOrPasswordAreInvalid -> 400
                            TokenCreationError.TokenLimitReached -> 403
                            TokenCreationError.UserNotFound -> 404
                        },
                    ).body(
                        mapOf(
                            "error" to
                                when (res.value) {
                                    TokenCreationError.UserOrPasswordAreInvalid -> Problem.userOrPasswordAreInvalid
                                    TokenCreationError.TokenLimitReached -> Problem.userNotAuthorized
                                    TokenCreationError.UserNotFound -> Problem.userNotFound
                                },
                        ),
                    )
        }
    }

    @GetMapping(UserUris.Users.GET_BY_ID)
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
                    ),
                )
            }

            is Failure ->
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
                                    UserGetByIdError.UserNotFound -> Problem.userNotFound
                                    UserGetByIdError.InvalidUserId -> Problem.invalidRequestContent
                                },
                        ),
                    )
        }
    }

    @PostMapping(UserUris.Users.INVITE)
    fun appInvite(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
    ): ResponseEntity<*> {
        val res = userService.createAppInvite(authenticatedUser.user.id)
        return when (res) {
            is Success ->
                ResponseEntity
                    .status(201)
                    .body(InviteAppOutputModel(res.value))

            is Failure ->
                ResponseEntity
                    .badRequest()
                    .body(
                        mapOf(
                            "error" to
                                when (res.value) {
                                    CreatingAppInviteError.UserNotFound -> Problem.userNotFound
                                    CreatingAppInviteError.CreatingInviteError -> Problem.inviteCreationError
                                },
                        ),
                    )
        }
    }
}
