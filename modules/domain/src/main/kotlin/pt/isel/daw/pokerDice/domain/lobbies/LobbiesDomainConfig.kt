package pt.isel.daw.pokerDice.domain.lobbies

import org.springframework.stereotype.Component

@Component
class LobbiesDomainConfig(
    // val isPrivate: Boolean,
    // val password: String?,
    val minUsersAllowed: Int,
    val maxUsersAllowed: Int,
    val minRoundsAllowed: Int,
    val maxRoundsAllowed: Int,
    val minCreditAllowed: Int,
) {
    init {
        require(minUsersAllowed in 2..maxUsersAllowed)
        require(maxUsersAllowed <= 6)
        require(minRoundsAllowed < maxRoundsAllowed)
        require(minRoundsAllowed >= 2)
        require(maxRoundsAllowed <= 10)
        require(minCreditAllowed >= 10)
        // require(isPrivate && password != null || !isPrivate && password == null)
    }
}

// TODO("resolver erros sintaxe ?")
