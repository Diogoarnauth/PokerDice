package pt.isel.daw.pokerDice.domain.games

import org.springframework.stereotype.Component
import pt.isel.daw.pokerDice.domain.lobbies.Lobby

@Component
class GameDomain {
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

    /** Rola os dados do turno atual, respeitando os dados “locked” */
    fun rollDice(
        turn: Turn,
        lockedIndices: List<Int>,
    ): Turn {
        TODO("Implementar lógica de rolar dados respeitando os índices bloqueados")
    }

    fun createGameFromLobby(
        lobby: Lobby,
        nrUsers: Int,
    ): Game =
        Game(
            id = java.util.UUID.randomUUID(),
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
