package pt.isel.daw.pokerDice.domain.invite

interface InviteEncoder {
    fun createValidationInformation(invite: String): InviteValidationInfo
}
