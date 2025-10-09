package pt.isel.daw.pokerDice.domain.games

import java.util.UUID

data class Game(
    val id: UUID,
    val state: State ,
    val nrPlayers: Int,
    val minCredits: Int,
// adicionar mais depois
) {
    enum class State {
        WAITING_FOR_PLAYERS,
        ENDED,
        RUNNING
        ;

        val isEnded: Boolean
            get() = this == ENDED

        val isRunning: Boolean
            get() = this == RUNNING

        val isWaitingForPlayers: Boolean
            get() = this == WAITING_FOR_PLAYERS
    }
}