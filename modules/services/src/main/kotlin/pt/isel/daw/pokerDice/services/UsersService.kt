package pt.isel.daw.pokerDice.services

import jakarta.inject.Named
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.springframework.web.bind.annotation.PathVariable
import pt.isel.daw.pokerDice.domain.invite.InviteDomain
import pt.isel.daw.pokerDice.domain.users.Token
import pt.isel.daw.pokerDice.domain.users.User
import pt.isel.daw.pokerDice.domain.users.UsersDomain
import pt.isel.daw.pokerDice.repository.*
import pt.isel.daw.pokerDice.utils.*

data class TokenExternalInfo(
    val tokenValue: String,
    val tokenExpiration: Instant,
)

sealed class UserCreationError {
    data object UserAlreadyExists : UserCreationError()

    data object InsecurePassword : UserCreationError()
}

typealias UserCreationResult = Either<UserCreationError, Int>

sealed class TokenCreationError {
    data object UserOrPasswordAreInvalid : TokenCreationError()
}
typealias TokenCreationResult = Either<TokenCreationError, TokenExternalInfo>


sealed class UserGetByIdError {
    data object UserNotFound : UserGetByIdError()
    //data class InvalidToken(val tokenValue: String) : UserGetByIdError() //dúvida :não sei se é necessário
}

typealias UserGetByIdResult = Either<UserGetByIdError, User>

typealias UserRegisterResult = Either<UserRegisterError, Int>

sealed class UserRegisterError {
    data object InvitationDontExist : UserRegisterError()

    data object InvitationExpired : UserRegisterError()

    data object InvitationUsed : UserRegisterError()

    data object UserAlreadyExists : UserRegisterError()

    data object InsecurePassword : UserRegisterError()
}

sealed class CreatingAppInviteError {
    data object CreatingInviteError : CreatingAppInviteError()
}

typealias CreatingAppInviteResult = Either<CreatingAppInviteError, String>



@Named // dúvida :falar com o stor acerca de usar service ou named
class UsersService(
    private val transactionManager: TransactionManager, // erro
    private val userDomain: UsersDomain,
    private val inviteDomain: InviteDomain,
    private val clock: Clock // erro
) {

    fun createAppInvite(userId: Int): CreatingAppInviteResult =
        transactionManager.run {
            val inviteRepository = it.inviteRepository
            val newInvite = inviteDomain.generateInviteValue()
            val inviteValidationInfo = inviteDomain.createInviteValidationInformation(newInvite)
            val state = inviteDomain.validState
            val now = clock.now()
            val invite = inviteRepository.createAppInvite(userId, inviteValidationInfo, state, now)
            if (invite == null) {
                failure(CreatingAppInviteError.CreatingInviteError)
            } else {
                success(newInvite)
            }
        }


    fun createUser(
        username: String,
        name: String,
        age: Int,
        password: String,
        inviteCode:String
    ): UserRegisterResult {

        if (!userDomain.isSafePassword(password)) {
            return failure(UserRegisterError.InsecurePassword)
        }

        val inviteCodeValidationInfo = inviteDomain.createInviteValidationInformation(inviteCode)
        val passwordValidationInfo = userDomain.createPasswordValidationInformation(password)

        return transactionManager.run {

            val usersRepository = it.usersRepository
            val inviteRepository = it.inviteRepository
            val invite = inviteRepository.getAppInviteByValidationInfo(inviteCodeValidationInfo)

            if (usersRepository.isUserStoredByUsername(username)) {
                return@run failure(UserRegisterError.UserAlreadyExists)
            }

            if (invite == null) {
                failure(UserRegisterError.InvitationDontExist)
            } else if (!inviteDomain.isInviteCodeValid(invite.state)) {
                failure(UserRegisterError.InvitationUsed)
            } else if (!inviteDomain.isInviteTimeNotExpired(invite.createdAt, clock)) {
                inviteRepository.changeInviteState( invite.id, inviteDomain.expiredState)
                failure(UserRegisterError.InvitationExpired)
            } else {
                val userId = usersRepository.create(username,name,age,inviteCode, passwordValidationInfo)
                inviteRepository.changeInviteState( invite.id, inviteDomain.usedState)
                success(userId)
            }
        }

    }


    fun createToken(
        username: String,
        password: String,
    ): TokenCreationResult {
        if (username.isBlank() || password.isBlank()) {
            failure(TokenCreationError.UserOrPasswordAreInvalid)
        }
        return transactionManager.run {
            val usersRepository = it.usersRepository
            val user: User =
                usersRepository.getUserByUsername(username)
                    ?: return@run failure(TokenCreationError.UserOrPasswordAreInvalid)
            if (!userDomain.validatePassword(password, user.passwordValidation)) {
                if (!userDomain.validatePassword(password, user.passwordValidation)) {
                    return@run failure(TokenCreationError.UserOrPasswordAreInvalid)
                }
            }
            val tokenValue = userDomain.generateTokenValue()
            val now = clock.now()
            val newToken =
                Token(
                    userDomain.createTokenValidationInformation(tokenValue),
                    user.id,
                    createdAt = now,
                    lastUsedAt = now,
                )
            usersRepository.createToken(newToken, userDomain.maxNumberOfTokensPerUser)
            Either.Right(
                TokenExternalInfo(
                    tokenValue,
                    userDomain.getTokenExpiration(newToken),
                ),
            )
        }
    }

    fun getById(
        @PathVariable id: Int,
    ): UserGetByIdResult {
        return transactionManager.run {
            val usersRepository = it.usersRepository

            val user = usersRepository.getUserById(id)
                ?: return@run failure(UserGetByIdError.UserNotFound)

            success(user)
        }
    }

    fun getUserByToken(token: String): User? {
        if (!userDomain.canBeToken(token)) {
            return null
        }
        return transactionManager.run {
            val usersRepository = it.usersRepository
            val tokenValidationInfo = userDomain.createTokenValidationInformation(token)
            val userAndToken = usersRepository.getTokenByTokenValidationInfo(tokenValidationInfo)
            if (userAndToken != null && userDomain.isTokenTimeValid(clock, userAndToken.second)) {
                usersRepository.updateTokenLastUsed(userAndToken.second, clock.now())
                userAndToken.first
            } else {
                null
            }
        }
    }

    fun hasAnyUser(): Boolean = transactionManager.run {
        val usersRepository = it.usersRepository
        return@run usersRepository.countUsers() > 0
    }

    fun bootstrapFirstUser(username : String, name : String, age : Int, password : String): Int =
        transactionManager.run {
            val usersRepository = it.usersRepository
            val passwordValidationInfo = userDomain.createPasswordValidationInformation(password)
            usersRepository.create(
                username = username,
                name = name,
                age = age,
                inviteCode = "BOOTSTRAP",
                passwordValidationInfo = passwordValidationInfo
            )
        }








}
