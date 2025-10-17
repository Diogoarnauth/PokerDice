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
        if (userService.hasAnyUser()) {
            return Problem.response(403, Problem.userAlreadyExists)
        }
        val id = userService.bootstrapFirstUser(input.username, input.name, input.age, input.password)
        return ResponseEntity.ok(id)
    }

    @PostMapping("/deposit")
    fun deposit(
        @RequestBody input: DepositInputModel,
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
    ): ResponseEntity<*> {
        val res = userService.deposit(input.value, authenticatedUser.user)

        return when (res) {
            is Success ->
                ResponseEntity.ok("Deposit successful. New balance: ${res.value}")

            is Failure ->
                when (res.value) {
                    DepositError.InvalidAmount -> Problem.response(400, Problem.badDeposit)
                    DepositError.UserNotFound -> Problem.response(404, Problem.userNotFound)
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
                    .header(
                        "Location",
                        UserUris.Users.byId(res.value).toASCIIString(),
                    ).build<Unit>()

            is Failure ->
                when (res.value) {
                    UserRegisterError.InsecurePassword -> Problem.response(400, Problem.insecurePassword)
                    UserRegisterError.UserAlreadyExists -> Problem.response(400, Problem.userAlreadyExists)

                    UserRegisterError.InvitationDontExist -> Problem.response(400, Problem.invitationDontExist)
                    UserRegisterError.InvitationUsed -> Problem.response(400, Problem.invitationUsed)
                    UserRegisterError.InvitationExpired -> Problem.response(400, Problem.invitationExpired)
                }
        }
    }

    @PostMapping(UserUris.Users.TOKEN)
    fun token(
        // working
        @RequestBody input: UserCreateTokenInputModel,
    ): ResponseEntity<*> {
        val res = userService.createToken(input.username, input.password)
        return when (res) {
            is Success ->
                ResponseEntity
                    .status(200)
                    .body(UserTokenCreateOutputModel(res.value.tokenValue))

            is Failure ->
                when (res.value) {
                    TokenCreationError.UserOrPasswordAreInvalid ->
                        Problem.response(400, Problem.userOrPasswordAreInvalid)
                    TokenCreationError.TokenLimitReached ->
                        Problem.response(400, Problem.userNotAuthorized)
                    TokenCreationError.UserNotFound ->
                        Problem.response(404, Problem.userNotFound)
                }
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
                ResponseEntity
                    .status(200)
                    .body(
                        UserGetByIdOutputModel(
                            username = user.username,
                            name = user.name,
                        ),
                    )
            }

            is Failure ->
                when (res.value) {
                    UserGetByIdError.UserNotFound ->
                        Problem.response(404, Problem.userNotFound)

                    UserGetByIdError.InvalidUserId ->
                        Problem.response(400, Problem.invalidRequestContent)
                }
        }
    }

    @PostMapping(UserUris.Users.INVITE)
    fun appInvite(
        @AuthenticationPrincipal
        authenticatedUser: AuthenticatedUser,
    ): ResponseEntity<*> {
        val res = userService.createAppInvite(authenticatedUser.user.id)
        return when (res) {
            is Success ->
                ResponseEntity
                    .status(201)
                    .body(InviteAppOutputModel(res.value))

            is Failure ->
                Problem.response(400, Problem.inviteCreationError)
        }
    }
}
