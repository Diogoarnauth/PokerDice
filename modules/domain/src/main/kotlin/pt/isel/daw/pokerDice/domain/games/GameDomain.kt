package pt.isel.daw.pokerDice.domain.games

import org.springframework.stereotype.Component
import pt.isel.daw.pokerDice.domain.lobbies.Lobby

@Component
class GameDomain {
    private val faces = listOf("A", "K", "Q", "J", "10", "9")

    /** Inicializa a primeira ronda com os turnos dos jogadores */
    fun initFirstRound(
        game: Game,
        players: List<Int>,
    ): Round {
        TODO()
    }

    fun initializeRoundsAndTurns(): List<Round> {
        TODO()
    }

    fun rollDice(currentTurn: Turn): String {
        val diceCount = 5
        val result = List(diceCount) { faces.random() }.joinToString("")
        println("rollDice - turn=$currentTurn -> $result")
        return result
    }

    fun rerollDice(
        currentTurn: Turn,
        idxToReroll: List<Int>,
    ): String {
        val previous =
            currentTurn.diceFaces?.split(",")?.map { it.trim() }
                ?: throw IllegalStateException("No previous dice found for this turn")

        // Rerolla apenas os índices indicados
        val newDice =
            previous.mapIndexed { index, oldFace ->
                if (index in idxToReroll) faces.random() else oldFace
            }

        val result = newDice.joinToString(",")
        println("🎲 Re-rolled dice for player ${currentTurn.playerId}: $result")

        return result
    }

    fun createGameFromLobby(
        lobby: Lobby,
        nrUsers: Int,
    ): Game =
        Game(
            lobbyId = lobby.id,
            state = Game.GameStatus.RUNNING,
            roundCounter = 0,
            gameWinner = null,
            nrUsers = nrUsers,
        )
// TODO ("TMB PODE COMEÇAR O JOGO QUANDO PASSAR O TEMPO E JA TIVER O N MIN)

/** Finaliza o turno do jogador e retorna se a ronda acabou */
    fun endTurn(round: Round): Boolean {
        TODO()
    }

/** Verifica se o jogo acabou */
    fun isGameFinished(
        game: Game,
        minRounds: Int = 1,
    ): Boolean {
        TODO()
    }

/** Calcula vencedor da ronda (poderia ser usado internamente) */
    fun calculateRoundWinner(round: Round): Int {
        TODO()
    }

/** Avalia a “mão” de um turno (simplificado, implementar poker dice real) */
    private fun evaluateHand(dice: List<Int>): Int {
        TODO()
    }
}
