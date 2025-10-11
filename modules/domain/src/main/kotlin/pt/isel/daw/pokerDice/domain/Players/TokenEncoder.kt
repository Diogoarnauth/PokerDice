package pt.isel.daw.pokerDice.domain.players

interface TokenEncoder {
    fun createValidationInformation(token: String): TokenValidationInfo
}
