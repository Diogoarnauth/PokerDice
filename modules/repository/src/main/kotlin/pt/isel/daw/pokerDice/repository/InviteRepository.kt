package pt.isel.daw.pokerDice.repository

import kotlinx.datetime.Instant
import pt.isel.daw.pokerDice.domain.invite.AppInvite
import pt.isel.daw.pokerDice.domain.invite.InviteValidationInfo


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

