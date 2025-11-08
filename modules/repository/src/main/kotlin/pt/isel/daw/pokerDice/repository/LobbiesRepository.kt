package pt.isel.daw.pokerDice.repository

import pt.isel.daw.pokerDice.domain.lobbies.Lobby

interface LobbiesRepository {
    fun getLobbiesNotFull(): List<Lobby>

    fun getById(id: Int): Lobby?

    fun existsByHost(hostId: Int): Boolean

    fun markGameAsStartedInLobby(lobbyId: Int)

    fun createLobby(
        hostId: Int,
        name: String,
        description: String,
        minPlayers: Int,
        maxPlayers: Int,
        rounds: Int,
        minCreditToParticipate: Int,
    ): Int?

    fun markLobbyAsAvailable(lobbyId: Int)

    fun deleteLobbyById(lobbyId: Int)
}
