package pt.isel.daw.pokerDice.repository

interface Transaction {
    val usersRepository: UsersRepository
    val inviteRepository: InviteRepository
    val lobbiesRepository: LobbiesRepository
    val gamesRepository: GamesRepository

    // val gamesRepository: GamesRepository

    // other repository types
    fun rollback()
}
