package pt.isel.daw.pokerDice.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.daw.pokerDice.domain.invite.AppInvite
import pt.isel.daw.pokerDice.domain.invite.InviteValidationInfo
import pt.isel.daw.pokerDice.repository.InviteRepository
import java.time.Instant

class JdbiInviteRepository(
    private val handle: Handle,
) : InviteRepository {
    override fun getAppInviteByValidationInfo(inviteValidationInfo: InviteValidationInfo): AppInvite? =
        handle
            .createQuery("select * from dbo.app_invite where invitevalidationinfo = :inviteValidationInfo")
            .bind("inviteValidationInfo", inviteValidationInfo.validationInfo)
            .mapTo<AppInviteModel>()
            .singleOrNull()
            ?.appInvite

    override fun changeInviteState(
        inviteId: Int,
        state: String,
    ): Int =
        handle
            .createUpdate("update dbo.app_invite set state = :state where id = :id")
            .bind("state", state)
            .bind("id", inviteId)
            .execute()

    override fun createAppInvite(
        inviterId: Int,
        inviteValidationInfo: InviteValidationInfo,
        state: String,
        createdAt: Instant,
    ): String? =

        handle
            .createUpdate(
                "insert into dbo.app_invite(inviterid, invitevalidationinfo, state, createdat) " +
                    "values (:inviterId, :inviteValidationInfo, :state, :createdAt)",
            ).bind("inviterId", inviterId)
            .bind("inviteValidationInfo", inviteValidationInfo.validationInfo)
            .bind("state", state)
            .bind("createdAt", createdAt.epochSecond)
            .executeAndReturnGeneratedKeys("id")
            .mapTo<String>()
            .one()

    private data class AppInviteModel(
        val id: Int,
        val inviterId: Int,
        val inviteValidationInfo: String,
        val state: String,
        val createdAt: Instant,
    ) {
        val appInvite: AppInvite
            get() = AppInvite(id, inviterId, InviteValidationInfo(inviteValidationInfo), state, createdAt)
    }
}
