package pt.isel.daw.pokerDice.domain.users

interface TokenEncoder {
    fun createValidationInformation(token: String): TokenValidationInfo
}
