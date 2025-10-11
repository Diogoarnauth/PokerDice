package pt.isel.daw.pokerDice.repository


interface Transaction {
    val playersRepository: PlayersRepository

   // val gamesRepository: GamesRepository

    // other repository types
    fun rollback()
}
