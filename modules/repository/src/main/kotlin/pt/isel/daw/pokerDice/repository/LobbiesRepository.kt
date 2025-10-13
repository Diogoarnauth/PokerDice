package pt.isel.daw.pokerDice.repository

import pt.isel.daw.pokerDice.domain.lobbies.Lobby
import pt.isel.daw.pokerDice.domain.players.PasswordValidationInfo

interface LobbiesRepository {

    fun getLobbiesNotFull(): List<Lobby>

    fun getById(id: Int): Lobby?

    fun existsByHost(hostId: Int):Boolean

    fun createLobby(
        hostId: Int,
        name: String,
        description: String,
        isPrivate: Boolean,
        passwordValidationInfo: PasswordValidationInfo?,
        minPlayers: Int,
        maxPlayers: Int,
        rounds: Int,
        minCreditToParticipate: Int
    ): Int?

    fun deleteLobbyById(lobbyId: Int)


    }
