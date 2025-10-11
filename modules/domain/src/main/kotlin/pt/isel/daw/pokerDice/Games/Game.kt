package pt.isel.daw.pokerDice.domain.games

import pt.isel.daw.pokerDice.domain.players.Player
import java.util.UUID

data class Game(
    val id: UUID,
    val state: State,
    val nrPlayers: Int,
    val minCredits: Int,
    val players: List<Player> = emptyList(),
    val rounds: List<Round> = emptyList(),
    val currentPlayerIndex: Int,
    val lastRoll: List<Dice>,
    val lastCombination:CombinationType?
// adicionar mais depois
) {
    enum class State {
        WAITING_FOR_PLAYERS,
        ENDED,
        RUNNING,
        NEXT_PLAYER
        ;

        val isEnded: Boolean
            get() = this == ENDED

        val isRunning: Boolean
            get() = this == RUNNING

        val isWaitingForPlayers: Boolean
            get() = this == WAITING_FOR_PLAYERS

        val isNextPlayer: Boolean
            get() = this == NEXT_PLAYER
    }
}