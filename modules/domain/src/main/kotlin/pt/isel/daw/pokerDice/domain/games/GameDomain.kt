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
            gameWinner = null,
            nrUsers = nrUsers,
        )
// TODO ("TMB PODE COMEÇAR O JOGO QUANDO PASSAR O TEMPO E JA TIVER O N MIN)
}
