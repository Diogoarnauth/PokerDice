package pt.isel.daw.pokerDice.http.model.LobbyModel

import java.util.UUID


data class LobbyGetByIdOutputModel(
    val id: Int,
    val name: String,
    val description: String,
    val hostId: Int,
    val isPrivate: Boolean,
    val minUsers: Int,
    val maxUsers: Int,
    val rounds: Int,
    val minCreditToParticipate: Int
)
