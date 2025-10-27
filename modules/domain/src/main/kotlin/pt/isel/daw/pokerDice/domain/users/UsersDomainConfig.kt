package pt.isel.daw.pokerDice.domain.users

import kotlin.time.Duration

data class UsersDomainConfig(
    val tokenSizeInBytes: Int,
    val tokenTtl: Duration,
    val tokenRollingTtl: Duration,
    val maxTokensPerUser: Int,
    val minUsernameLength: Int,
    val minPasswordLength: Int,
    val minAge: Int,
    val maxAge: Int,
) {
    init {
        require(tokenSizeInBytes > 0)
        require(tokenTtl.isPositive())
        require(tokenRollingTtl.isPositive())
        require(maxTokensPerUser > 0)

        require(minUsernameLength > 2)
        require(minPasswordLength >= 4)
        require(minAge >= 18)
        require(maxAge > minAge)
    }
}
