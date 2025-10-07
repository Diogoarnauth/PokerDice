package org.example.repository



import org.example.Domain.Games.Game
import java.util.UUID

interface GamesRepository {
    fun insert(game: Game)

    fun getById(id: UUID): Game?

    fun update(game: Game)
}
