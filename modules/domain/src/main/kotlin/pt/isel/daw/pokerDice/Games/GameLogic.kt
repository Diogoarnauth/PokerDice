package pt.isel.daw.pokerDice.domain.games

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import pt.isel.daw.pokerDice.domain.players.Player
import java.util.UUID
import kotlin.random.Random
import kotlin.time.Duration

//organizar isto noutro sitio
enum class CombinationType {
    HIGH_CARD, ONE_PAIR, TWO_PAIR, THREE_OF_A_KIND, FULL_HOUSE, FOUR_OF_A_KIND, FIVE_OF_A_KIND
}


class GameLogic(
    private val clock: Clock,
    private val duration: Duration
){

    /**
     * Cria um novo game ainda sem jogadores
     */
    fun createNewGame (nrPlayers : Int, minCredits: Int): Game {
        return Game(
            id = UUID.randomUUID(),
            state = Game.State.WAITING_FOR_PLAYERS,
            nrPlayers = nrPlayers,
            minCredits = minCredits,
            players = emptyList(),
            currentPlayerIndex = 0,
            lastRoll = emptyList(),
            lastCombination = null
        )
    }

    /**
     * Adiciona, se possível, um novo jogador ao jogo.
     */
    fun addPlayer (game: Game, playerCredits: Int, newPlayer: Player): GameResult {
        if (game.state != Game.State.WAITING_FOR_PLAYERS){
            return GameResult.InvalidState(" It is not possible to add players right now.")
        }

        if (game.minCredits > playerCredits){
            return GameResult.NotEnoughCredits("It is necessary more credits to play this game.", game.minCredits)
        }

        if (game.players.any{it.id == newPlayer.id }){
            return GameResult.PlayerAlreadyExistInGame
        }

        if (game.players.size >= game.nrPlayers){
            return GameResult.GameFull
        }

        // guardar temporariamente o número de jogadores atuais
        val updatedPlayers = game.players + newPlayer
        val updatedGame =
            if (updatedPlayers.size == game.nrPlayers){
                // todos os jogadores estão prontos -> o jogo começa
                game.copy(
                    players = updatedPlayers,
                    state = Game.State.RUNNING,
                )

            } else {
                game.copy(players = updatedPlayers)
            }

        return if( updatedGame.state == Game.State.RUNNING){
            GameResult.GameStarted(updatedGame)
        } else{
            GameResult.WaitingForMorePlayers(updatedGame)
        }
    }

    /**
     * Aplica uma jogada (round). Para já, apenas faz validações básicas.
     * Mais tarde, vai ser expandido com a lógica do lançamento de dados.
     */
    fun applyRound(game: Game, player: Player): GameResult {
        if (player.id !in game.players.map { it.id }){
            return GameResult.NotAPlayer
        }

        if (!game.state.isRunning || game.state != Game.State.NEXT_PLAYER){
            return GameResult.InvalidState(" The game hasn't started, or it's already ended.")
        }



        val currentIndex = game.currentPlayerIndex
        val currentPlayer = game.players[currentIndex]
        if (player.id != currentPlayer.id){
            return GameResult.InvalidState("It is not your turn.")
        }

        val now = clock.now()

        //Aqui vamos tratar da lógica dos lançamentos e transição dos turnos
        val roll = List(5){ Dice.random(Random.Default) }

        //Determinar a combinação
        val combination = determineCombination(roll)

        //Passar para o próximo jogador (ou terminar jogo)
        val isLastPlayer = currentIndex == game.players.lastIndex
        val nextState = if (isLastPlayer) Game.State.ENDED else Game.State.NEXT_PLAYER
        val nextIndex = if (isLastPlayer) 0 else currentIndex + 1


        val updatedGame = game.copy(
            state = nextState,
            currentPlayerIndex = nextIndex,
            lastRoll = roll,
            lastCombination = combination
        )

        return if (nextState == Game.State.ENDED)
            GameResult.GameEnded(updatedGame, now)
        else
            GameResult.RoundApplied(updatedGame, now)
    }

    /**
    * Determina o tipo de combinação obtida.
    */
    private fun determineCombination(roll: List<Dice>): CombinationType {
        val counts = roll.groupingBy { it.value }.eachCount().values.sortedDescending()
        return when {
            counts == listOf(5) -> CombinationType.FIVE_OF_A_KIND
            counts == listOf(4, 1) -> CombinationType.FOUR_OF_A_KIND
            counts == listOf(3, 2) -> CombinationType.FULL_HOUSE
            counts == listOf(3, 1, 1) -> CombinationType.THREE_OF_A_KIND
            counts == listOf(2, 2, 1) -> CombinationType.TWO_PAIR
            counts == listOf(2, 1, 1, 1) -> CombinationType.ONE_PAIR
            else -> CombinationType.HIGH_CARD
        }
    }

}

sealed class GameResult {
    data class WaitingForMorePlayers(val game: Game) : GameResult()
    data class GameStarted(val game: Game) : GameResult()
    data class RoundApplied(val game: Game, val time: Instant) : GameResult()
    data class NotEnoughCredits(val reason: String,val minRequired: Int) : GameResult()
    data class InvalidState(val reason: String) : GameResult()
    data class GameEnded (val game: Game, val time: Instant) : GameResult()

    data object PlayerAlreadyExistInGame : GameResult()
    data object GameFull : GameResult()
    data object NotAPlayer : GameResult()
}


