package pt.isel.daw.pokerDice.repository

import kotlinx.datetime.Instant
import pt.isel.daw.pokerDice.domain.Invite.AppInvite
import pt.isel.daw.pokerDice.domain.Invite.InviteValidationInfo


interface InviteRepository {
    fun getAppInviteByValidationInfo(inviteValidationInfo: InviteValidationInfo): AppInvite?

    fun changeInviteState(
        inviteId: Int,
        state: String,
    ): Int

    fun createAppInvite(
        inviterId: Int,
        inviteValidationInfo: InviteValidationInfo,
        state: String,
        createdAt: Instant,
    ): String?
}

