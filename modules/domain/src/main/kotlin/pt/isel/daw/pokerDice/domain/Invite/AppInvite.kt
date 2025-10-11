package pt.isel.daw.pokerDice.domain.Invite

import kotlinx.datetime.Instant

data class AppInvite(
    val id: Int,
    val inviterId: Int,
    val inviteValidationInfo: InviteValidationInfo,
    val state: String,
    val createdAt: Instant,
)
