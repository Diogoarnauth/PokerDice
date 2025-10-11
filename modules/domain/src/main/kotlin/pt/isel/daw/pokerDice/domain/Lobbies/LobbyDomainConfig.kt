package pt.isel.daw.pokerDice.domain.Lobbies

import org.springframework.stereotype.Component

@Component
class LobbiesDomainConfig(
    val isPrivate: Boolean,
    val password: String?,
    val minPlayersAllowed: Int,
    val maxPlayersAllowed: Int,
    val minRoundsAllowed: Int,
    val maxRoundsAllowed: Int,
    val minCreditAllowed: Int,
){
    init{
        require (minPlayersAllowed > 1 && minPlayersAllowed <= maxPlayersAllowed)
        require (maxPlayersAllowed <= 6)
        require(minRoundsAllowed < maxRoundsAllowed)
        require(minRoundsAllowed >= 2 )
        require(maxRoundsAllowed <= 10 )
        require(minCreditAllowed >= 10)
        require(isPrivate && password != null || !isPrivate && password == null)
    }

}
