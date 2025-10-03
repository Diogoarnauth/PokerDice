package org.example.Domain.Games

import java.util.UUID

data class Game(
    val id: UUID,
    val state: State ,
    val NrPlayers: Int,
    val MinCredits: Int,
// adicionar mais depois
) {
    enum class State {
        ENDED,
        RUNNING
        ;

        val isEnded: Boolean
            get() = this == ENDED
    }
}