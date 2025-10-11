package pt.isel.daw.pokerDice.repository

import kotlinx.datetime.Instant
import pt.isel.daw.pokerDice.domain.Lobbies.Lobby
import pt.isel.daw.pokerDice.domain.players.PasswordValidationInfo
import pt.isel.daw.pokerDice.domain.players.Player
import pt.isel.daw.pokerDice.domain.players.Token
import pt.isel.daw.pokerDice.domain.players.TokenValidationInfo

interface LobbiesRepository {

    fun getLobbiesNotFull(): List<Lobby>

    fun getById(id: Int): Lobby?

    }
