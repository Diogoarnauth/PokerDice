package org.example.repository

interface Transaction {
    val playersRepository: PlayersRepository

    val gamesRepository: GamesRepository

    // other repository types
    fun rollback()
}
