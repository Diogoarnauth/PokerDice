package pt.isel.daw.pokerDice.repository.jdbi

import org.jdbi.v3.core.Handle
import pt.isel.daw.pokerDice.repository.GamesRepository
import pt.isel.daw.pokerDice.repository.InviteRepository
import pt.isel.daw.pokerDice.repository.LobbiesRepository
import pt.isel.daw.pokerDice.repository.RoundRepository
import pt.isel.daw.pokerDice.repository.Transaction
import pt.isel.daw.pokerDice.repository.UsersRepository

class JdbiTransaction(
    private val handle: Handle,
) : Transaction {
    override val usersRepository: UsersRepository = JdbiUsersRepository(handle)
    override val lobbiesRepository: LobbiesRepository = JdbiLobbyRepository(handle)
    override val gamesRepository: GamesRepository = JdbiGamesRepository(handle)
    override val inviteRepository: InviteRepository = JdbiInviteRepository(handle)
    override val roundRepository: RoundRepository = JdbiRoundRepository(handle)

    override fun rollback() {
        handle.rollback()
    }
}
