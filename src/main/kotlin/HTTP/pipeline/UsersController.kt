package org.example.HTTP.pipeline

import com.sun.net.httpserver.Authenticator
import org.example.HTTP.model.GetByIdOutputModel
import org.example.HTTP.model.Problem
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

/*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pt.isel.daw.tictactoe.domain.users.AuthenticatedUser
import pt.isel.daw.tictactoe.http.model.Problem
import pt.isel.daw.tictactoe.http.model.UserCreateInputModel
import pt.isel.daw.tictactoe.http.model.UserCreateTokenInputModel
import pt.isel.daw.tictactoe.http.model.UserHomeOutputModel
import pt.isel.daw.tictactoe.http.model.UserTokenCreateOutputModel
import pt.isel.daw.tictactoe.services.TokenCreationError
import pt.isel.daw.tictactoe.services.UserCreationError
import pt.isel.daw.tictactoe.services.UsersService
import pt.isel.daw.tictactoe.utils.Failure
import pt.isel.daw.tictactoe.utils.Success
*/
@RestController
class UsersController(
    private val userService: UsersService,
) {
    /*@PostMapping(Uris.Users.CREATE)
    fun create(
        @RequestBody input: UserCreateInputModel,
    ): ResponseEntity<*> {
        val res = userService.createUser(input.username, input.password)
        return when (res) {
            is Success ->
                ResponseEntity.status(201)
                    .header(
                        "Location",
                        Uris.Users.byId(res.value).toASCIIString(),
                    ).build<Unit>()

            is Failure ->
                when (res.value) {
                    UserCreationError.InsecurePassword -> Problem.response(400, Problem.insecurePassword)
                    UserCreationError.UserAlreadyExists -> Problem.response(400, Problem.userAlreadyExists)
                }
        }
    }

    @PostMapping(Uris.Users.TOKEN)
    fun token(
        @RequestBody input: UserCreateTokenInputModel,
    ): ResponseEntity<*> {
        val res = userService.createToken(input.username, input.password)
        return when (res) {
            is Success ->
                ResponseEntity.status(200)
                    .body(UserTokenCreateOutputModel(res.value.tokenValue))

            is Failure ->
                when (res.value) {
                    TokenCreationError.UserOrPasswordAreInvalid ->
                        Problem.response(400, Problem.userOrPasswordAreInvalid)
                }
        }
    }

    @PostMapping(Uris.Users.LOGOUT)
    fun logout(user: AuthenticatedUser) {
        userService.revokeToken(user.token)
    }
*/
    @GetMapping(Uris.Users.GET_BY_ID)
    fun getById(@PathVariable id: String): ResponseEntity<*> {
        // 1) Validar o path param (tem de ser número)
        val userId = id.toIntOrNull()
            ?: return Problem.response(400, Problem.invalidRequestContent)

        // 2) Pedir ao serviço
        val res = usersService.getById(userId)

        // 3) Mapear resultado → HTTP
        return when (res) {
            is Authenticator.Success -> ResponseEntity.ok(
                GetByIdOutputModel(
                    id = res.value.id,          // adapta o tipo se for Long
                    username = res.value.username
                )
            )
            is Authenticator.Failure -> ResponseEntity.status(404).build<Unit>() // not found
        }
    }

/*
    @GetMapping(Uris.Users.HOME)
    fun getUserHome(userAuthenticatedUser: AuthenticatedUser): UserHomeOutputModel {
        return UserHomeOutputModel(
            id = userAuthenticatedUser.user.id,
            username = userAuthenticatedUser.user.username,
        )
    }*/
}