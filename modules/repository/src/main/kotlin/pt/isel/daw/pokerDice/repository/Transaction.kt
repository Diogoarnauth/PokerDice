package pt.isel.daw.pokerDice.repository


interface Transaction {
    val playersRepository: PlayersRepository
    val inviteRepository: InviteRepository

   // val gamesRepository: GamesRepository

    // other repository types
    fun rollback()
}
