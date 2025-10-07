package org.example.repository

interface Transaction {
    val usersRepository: UsersRepository

    val gamesRepository: GamesRepository

    // other repository types
    fun rollback()
}
