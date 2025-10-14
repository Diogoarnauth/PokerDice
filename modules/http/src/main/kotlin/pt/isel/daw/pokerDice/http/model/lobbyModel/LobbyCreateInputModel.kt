package pt.isel.daw.pokerDice.http.model.lobbyModel

import pt.isel.daw.pokerDice.domain.users.PasswordValidationInfo

class LobbyCreateInputModel(
    val name: String,
    val description: String,
    // val hostId: Int
    val isPrivate: Boolean,
    val passwordValidationInfo: PasswordValidationInfo?,
    val minUsers: Int,
    val maxUsers: Int,
    val rounds: Int,
    val minCreditToParticipate: Int,
)
