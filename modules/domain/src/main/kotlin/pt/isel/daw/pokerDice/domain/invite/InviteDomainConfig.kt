package pt.isel.daw.pokerDice.domain.invite

import kotlin.time.Duration

data class InviteDomainConfig(
    val expireInviteTime: Duration,
    val validState: String,
    val expiredState: String,
    val usedState: String,
    val declinedState: String,
) {
    init {
        require(expireInviteTime.isPositive())
        require(validState.isNotBlank())
    }
}
