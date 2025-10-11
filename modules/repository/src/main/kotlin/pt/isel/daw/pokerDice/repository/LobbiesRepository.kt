package pt.isel.daw.pokerDice.repository

import pt.isel.daw.pokerDice.domain.lobbies.Lobby

interface LobbiesRepository {

    fun getLobbiesNotFull(): List<Lobby>

    fun getById(id: Int): Lobby?

    }
