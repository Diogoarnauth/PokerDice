package pt.isel.daw.pokerDice.domain.games

/*
data class Game(
    val id: UUID,
    val lobby_id: UUID,
    val state: State,
    val nrPlayers: Int,
    // val minCredits: Int,
    val players: List<User> = emptyList(),
    val rounds_counter: Int,
    val winner: Int? = null,
    val round_result: Int? = null,
    // val scores: MutableMap<User, Int> = mutableMapOf(),
    val currentPlayerIndex: Int,
    // val lastRoll: List<Dice>,
    // val lastCombination: CombinationType?,
) {
    enum class State {
        WAITING_FOR_PLAYERS,
        ENDED,
        RUNNING,
        NEXT_PLAYER,
        ROUND_OVER,
        ;

        val isEnded: Boolean get() = this == ENDED
        val isRunning: Boolean get() = this == RUNNING
        val isWaitingForPlayers: Boolean get() = this == WAITING_FOR_PLAYERS
        val isNextPlayer: Boolean get() = this == NEXT_PLAYER
        val isRoundOver: Boolean get() = this == ROUND_OVER
    }

    /**
 * Verifica se o jogo está completo (todos os jogadores entraram).
 */
    val isFull: Boolean get() = players.size >= nrPlayers

    /**
 * Retorna o jogador atual (de acordo com o índice).
 */
    val currentPlayer: User? get() = players.getOrNull(currentPlayerIndex)
}
*/

data class Game(
    val id: Int? = null,
    val lobbyId: Int,
    var state: GameStatus,
    var nrUsers: Int,
    var gameWinner: Int? = null,
    var roundCounter: Int,
) {
    enum class GameStatus {
        CLOSED,

        RUNNING,
        ;

        val isEnded: Boolean get() = this == GameStatus.CLOSED
        val isRunning: Boolean get() = this == GameStatus.RUNNING
    }
}
