package pt.isel.daw.pokerDice.domain.invite

import java.time.Instant

data class AppInvite(
    val id: Int,
    val inviterId: Int,
    val inviteValidationInfo: InviteValidationInfo,
    val state: String,
    val createdAt: Instant,
)
