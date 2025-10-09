package pt.isel.daw.pokerDice.repository
import pt.isel.daw.pokerDice.domain.games.Game
import java.util.UUID

interface GamesRepository {
    fun insert(game: Game)

    fun getById(id: UUID): Game?

    fun update(game: Game)
}
