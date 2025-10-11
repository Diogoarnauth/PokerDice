package pt.isel.daw.pokerDice.domain.players
import kotlin.time.Duration

data class PlayersDomainConfig(
    val tokenSizeInBytes: Int,
    val tokenTtl: Duration,
    val tokenRollingTtl: Duration,
    val maxTokensPerPlayer: Int,
) {
    init {
        require(tokenSizeInBytes > 0)
        require(tokenTtl.isPositive())
        require(tokenRollingTtl.isPositive())
        require(maxTokensPerPlayer > 0)
    }
}
