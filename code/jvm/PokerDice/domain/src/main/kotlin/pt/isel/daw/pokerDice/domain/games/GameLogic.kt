package pt.isel.daw.pokerDice.domain.games

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import pt.isel.daw.pokerDice.domain.games.Dice.Companion.faces
import pt.isel.daw.pokerDice.domain.lobbies.Lobby
import kotlin.time.Duration

// organizar isto noutro sitio
enum class CombinationType {
    HIGH_CARD,
    ONE_PAIR,
    TWO_PAIR,
    THREE_OF_A_KIND,
    FULL_HOUSE,
    FOUR_OF_A_KIND,
    FIVE_OF_A_KIND,
}

sealed class GameResult {
    // para que é necessario

    data class WaitingForMorePlayers(
        val game: Game,
    ) : GameResult()

    data class GameStarted(
        val game: Game,
    ) : GameResult()

    data class RoundApplied(
        val game: Game,
        val time: Instant,
    ) : GameResult()

    data class NotEnoughCredits(
        val reason: String,
        val minRequired: Int,
    ) : GameResult()

    data class InvalidState(
        val reason: String,
    ) : GameResult()

    data class GameEnded(
        val game: Game,
        val time: Instant,
    ) : GameResult()

    data object PlayerAlreadyExistInGame : GameResult()

    data object GameFull : GameResult()

    data object NotAPlayer : GameResult()
}

/*

object PokerDiceScorer {
    // testar depois esse score

    fun score(dice: List<Dice>): Double {
        require(dice.size == 5) { "A roll must contain exactly 5 dice" }

        val values = dice.map { it.value }.sortedDescending()

        val counts =
            values
                .groupingBy { it }
                .eachCount()

        val groups =
            counts.entries
                .sortedWith(
                    compareByDescending<Map.Entry<Int, Int>> { it.value }
                        .thenByDescending { it.key },
                )

        val isStraight = isStraight(values)

        return when {
            groups.first().value == 5 -> {
                // Five of a kind
                900 + groups.first().key / 100.0
            }

            groups.first().value == 4 -> {
                // Four of a kind + kicker
                val quad = groups.first().key
                val kicker = groups.find { it.value == 1 }?.key ?: 0
                800 + (quad * 10 + kicker) / 100.0
            }

            groups.first().value == 3 && groups[1].value == 2 -> {
                // Full house (trio + par)
                val trio = groups.first().key
                val pair = groups[1].key
                700 + (trio * 10 + pair) / 100.0
            }

            isStraight -> {
                val highCard = values.max()
                600 + highCard / 100.0
            }

            groups.first().value == 3 -> {
                // Three of a kind + kickers
                val trio = groups.first().key
                val kickers = groups.filter { it.value == 1 }.map { it.key }.sortedDescending()
                val sumKickers = kickers.sum()
                500 + (trio * 10 + sumKickers) / 100.0
            }

            groups.first().value == 2 && groups[1].value == 2 -> {
                // Two pairs + kicker
                val pairHigh = maxOf(groups[0].key, groups[1].key)
                val pairLow = minOf(groups[0].key, groups[1].key)
                val kicker = groups.find { it.value == 1 }?.key ?: 0
                400 + (pairHigh * 10 + pairLow + kicker / 10.0) / 100.0
            }

            groups.first().value == 2 -> {
                // One pair + kickers
                val pair = groups.first().key
                val kickers = groups.filter { it.value == 1 }.map { it.key }.sortedDescending()
                val sumKickers = kickers.sum()
                300 + (pair * 10 + sumKickers) / 100.0
            }

            else -> {
                // High card
                val high = values.first()
                200 + high / 100.0
            }
        }
    }

    private fun isStraight(values: List<Int>): Boolean {
        val sorted = values.sorted()
        return sorted.zipWithNext().all { (a, b) -> b - a == 1 }
    }
}
*/

class GameLogic(
    private val clock: Clock,
    private val duration: Duration,
) {
    fun createGame(
        lobbyId: Int,
        nrUsers: Int,
    ): Game {
        require(nrUsers >= 2) { "Um jogo precisa de pelo menos dois jogadores." }

        return Game(
            id = null,
            lobbyId = lobbyId,
            state = Game.GameStatus.RUNNING,
            nrUsers = nrUsers,
            roundCounter = 0,
        )
    }

    fun rollDice(currentTurn: Turn): String {
        val diceCount = 5
        val result = List(diceCount) { faces.random() }.joinToString(",")
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

        // Reroll apenas dos índices indicados
        val newDice =
            previous.mapIndexed { index, oldFace ->
                if (index in idxToReroll) faces.random() else oldFace
            }

        val result = newDice.joinToString(",")
        println("Re-rolled dice for player ${currentTurn.playerId}: $result")

        return result
    }

    fun evaluateRoundWinner(roundId: Int): Int {
        // TODO: lógica real baseada nas combinações de dados
        // Por agora, devolve aleatoriamente um player
        println("A avaliar vencedor da ronda $roundId")
        return (1..4).random()
    }

    fun createGameFromLobby(
        lobby: Lobby,
        nrUsers: Int,
    ): Game =
        Game(
            lobbyId = lobby.id,
            state = Game.GameStatus.RUNNING,
            roundCounter = 0,
            nrUsers = nrUsers,
        )
}
