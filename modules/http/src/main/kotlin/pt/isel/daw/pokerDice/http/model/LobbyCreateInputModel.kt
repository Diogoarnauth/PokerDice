package pt.isel.daw.pokerDice.http.model

import pt.isel.daw.pokerDice.domain.players.PasswordValidationInfo

class LobbyCreateInputModel (
    val name: String,
    val description: String,
    //val hostId: Int
    val isPrivate: Boolean,
    val passwordValidationInfo: PasswordValidationInfo?,
    val minPlayers : Int,
    val maxPlayers : Int,
    val rounds : Int,
    val minCreditToParticipate: Int
)
