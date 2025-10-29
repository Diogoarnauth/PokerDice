package pt.isel.daw.pokerDice.services

import jakarta.inject.Named
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import pt.isel.daw.pokerDice.domain.invite.InviteDomain
import pt.isel.daw.pokerDice.domain.users.Token
import pt.isel.daw.pokerDice.domain.users.User
import pt.isel.daw.pokerDice.domain.users.UsersDomain
import pt.isel.daw.pokerDice.repository.TransactionManager
import pt.isel.daw.pokerDice.utils.Either
import pt.isel.daw.pokerDice.utils.failure
import pt.isel.daw.pokerDice.utils.success

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

    data object UserNotFound : TokenCreationError()

    data object TokenLimitReached : TokenCreationError()
}

typealias TokenCreationResult = Either<TokenCreationError, TokenExternalInfo>

sealed class UserGetByIdError {
    data object UserNotFound : UserGetByIdError()

    data object InvalidUserId : UserGetByIdError()
    // data class InvalidToken(val tokenValue: String) : UserGetByIdError() //dúvida :não sei se é necessário
}

sealed class UserRegisterError {
    data object AdminAlreadyExists : UserRegisterError()

    data object InvitationDontExist : UserRegisterError()

    data object InvitationExpired : UserRegisterError()

    data object InvitationUsed : UserRegisterError()

    data object UserAlreadyExists : UserRegisterError()

    data object InvalidData : UserRegisterError()

    // Novos Erros de Validação
    data object InvalidUsername : UserRegisterError()

    data object InvalidName : UserRegisterError()

    data object InvalidAge : UserRegisterError()

    data object InsecurePassword : UserRegisterError()
}
typealias UserRegisterResult = Either<UserRegisterError, Int>

sealed class DepositError {
    data object InvalidAmount : DepositError() // TODO(melhorar isto)

    data object UserNotFound : DepositError()
}

typealias UserGetByIdResult = Either<UserGetByIdError, User>

typealias DepositResult = Either<DepositError, Int>

sealed class InvalidInputError {
    data object InvalidInput : InvalidInputError()
}

sealed class CreatingAppInviteError {
    data object CreatingInviteError : CreatingAppInviteError()

    data object UserNotFound : CreatingAppInviteError()
}

typealias CreatingAppInviteResult = Either<CreatingAppInviteError, String>

// dúvida :falar com o stor acerca de usar service ou named
@Named
class UsersService(
    private val transactionManager: TransactionManager,
    private val userDomain: UsersDomain,
    private val inviteDomain: InviteDomain,
    private val clock: Clock,
) {
    fun bootstrapFirstUser(
        username: String,
        name: String,
        age: Int,
        password: String,
    ): UserRegisterResult {
        if (!userDomain.isAgeValid(age) || !userDomain.isSafePassword(password) ||
            !userDomain.isUsernameValid(username)
        ) {
            return failure(UserRegisterError.InvalidData) // TODO("VERIFICAR SE QUEREMOS ESTE NOME OU PASSWORD
            // / USERNAME / AGE")
        }
        if (hasAnyUser()) {
            return failure(UserRegisterError.AdminAlreadyExists)
        }

        return transactionManager.run {
            val usersRepository = it.usersRepository
            val passwordValidationInfo = userDomain.createPasswordValidationInformation(password)
            val userId =
                usersRepository.create(
                    username = username,
                    name = name,
                    age = age,
                    inviteCode = "BOOTSTRAP",
                    passwordValidationInfo = passwordValidationInfo,
                )
            success(userId)
        }
    }

    fun hasAnyUser(): Boolean =
        transactionManager.run {
            val usersRepository = it.usersRepository
            return@run usersRepository.countUsers() > 0
        }

    fun deposit(
        amount: Int,
        user: User,
    ): DepositResult {
        if (amount <= 0) {
            return failure(DepositError.InvalidAmount)
        }

        return transactionManager.run {
            val usersRepository = it.usersRepository

            val existingUser =
                usersRepository.getUserById(user.id)
                    ?: return@run failure(DepositError.UserNotFound)

            val newCredit = existingUser.credit + amount
            usersRepository.updateUserCredit(user.id, newCredit)

            success(newCredit)
        }
    }

    fun createAppInvite(userId: Int): CreatingAppInviteResult =
        transactionManager.run {
            it.usersRepository.getUserById(userId)
                ?: return@run failure(CreatingAppInviteError.UserNotFound)

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
        inviteCode: String,
    ): UserRegisterResult {
        if (!userDomain.isSafePassword(password)) {
            return failure(UserRegisterError.InsecurePassword)
        }
        if (!userDomain.isUsernameValid(username)) {
            return failure(UserRegisterError.InvalidUsername)
        }
        if (!userDomain.isNameValid(name)) {
            return failure(UserRegisterError.InvalidName)
        }
        if (!userDomain.isAgeValid(age)) {
            return failure(UserRegisterError.InvalidAge)
        }

        val inviteCodeValidationInfo = inviteDomain.createInviteValidationInformation(inviteCode)
        val passwordValidationInfo = userDomain.createPasswordValidationInformation(password)

        return transactionManager.run {
            val usersRepository = it.usersRepository
            val inviteRepository = it.inviteRepository

            if (usersRepository.isUserStoredByUsername(username)) {
                return@run failure(UserRegisterError.UserAlreadyExists)
            }

            val invite = inviteRepository.getAppInviteByValidationInfo(inviteCodeValidationInfo)

            if (usersRepository.isUserStoredByUsername(username)) {
                return@run failure(UserRegisterError.UserAlreadyExists)
            }

            if (invite == null) {
                return@run failure(UserRegisterError.InvitationDontExist)
            } else if (!inviteDomain.isInviteCodeValid(invite.state)) {
                return@run failure(UserRegisterError.InvitationUsed)
            }
            /*else if (!inviteDomain.isInviteTimeNotExpired(invite.createdAt, clock)) {
                inviteRepository.changeInviteState(invite.id, inviteDomain.expiredState)
                return@run failure(UserRegisterError.InvitationExpired)
            }*/

            val userId = usersRepository.create(username, name, age, inviteCode, passwordValidationInfo)

            inviteRepository.changeInviteState(invite.id, inviteDomain.usedState)
            success(userId)
        }
    }

    fun createToken(
        username: String,
        password: String,
    ): TokenCreationResult {
        if (username.isBlank() || password.isBlank()) {
            return failure(TokenCreationError.UserOrPasswordAreInvalid)
        }

        return transactionManager.run {
            val usersRepository = it.usersRepository

            val user: User =
                usersRepository.getUserByUsername(username)
                    ?: return@run failure(TokenCreationError.UserOrPasswordAreInvalid)

            if (!userDomain.validatePassword(password, user.passwordValidation)) {
                return@run failure(TokenCreationError.UserOrPasswordAreInvalid)
            }

            val existingTokens = usersRepository.getUserTokens(user.id)
            val maxTokens = userDomain.maxNumberOfTokensPerUser

            if (existingTokens.size >= maxTokens) {
                // Marque o token mais antigo como expirado
                val oldestToken = existingTokens.minByOrNull { it.createdAt }
                if (oldestToken != null) {
                    usersRepository.removeTokenByValidationInfo(oldestToken.tokenValidationInfo)
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
            success(
                TokenExternalInfo(
                    tokenValue,
                    userDomain.getTokenExpiration(newToken),
                ),
            )
        }
    }

    fun getById(id: Int): UserGetByIdResult {
        return transactionManager.run {
            val usersRepository = it.usersRepository

            val user =
                usersRepository.getUserById(id)
                    ?: return@run failure(UserGetByIdError.UserNotFound)
            println("user $user")
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
}
