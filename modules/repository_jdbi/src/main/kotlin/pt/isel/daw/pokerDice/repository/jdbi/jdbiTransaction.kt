package pt.isel.daw.pokerDice.repository.jdbi

import org.jdbi.v3.core.Handle
import pt.isel.daw.pokerDice.repository.InviteRepository

import pt.isel.daw.pokerDice.repository.PlayersRepository
import pt.isel.daw.pokerDice.repository.Transaction


class JdbiTransaction(
    private val handle: Handle,
) : Transaction {
    override val playersRepository: PlayersRepository = JdbiPlayersRepository(handle)
   // override val gamesRepository: GamesRepository = JdbiGamesRepository(handle)
    override val inviteRepository: InviteRepository= JdbiInviteRepository(handle)
    override fun rollback() {
        handle.rollback()
    }
}
