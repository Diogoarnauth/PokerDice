package pt.isel.daw.pokerDice.domain.Invite


interface InviteEncoder {
    fun createValidationInformation(invite: String): InviteValidationInfo
}
