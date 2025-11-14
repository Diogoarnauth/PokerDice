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

    /*
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
     */

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

    fun evaluateRoundWinner(listPlayerPlay: List<Pair<Int, String>>): Int {
        println("A avaliar vencedor da ronda")

        if (listPlayerPlay.isEmpty()) {
            throw IllegalArgumentException("Nenhum jogador para avaliar")
        }

        // Calcula a pontuação de cada jogador
        val playersWithScores =
            listPlayerPlay.map { (playerId, diceResult) ->
                val score = calculateDiceScore(diceResult)
                Triple(playerId, diceResult, score)
            }

        // Encontra a pontuação máxima
        val maxScore = playersWithScores.maxOf { it.third }

        // Retorna o jogador com a maior pontuação
        // Em caso de empate, retorna o primeiro
        return playersWithScores.first { it.third == maxScore }.first
    }

    private fun calculateDiceScore(diceResult: String): Int {
        if (diceResult.isEmpty()) return 0

        // Converte a string de dados em lista de faces
        val dice = diceResult.split(",").map { it.trim() }

        if (dice.size != 5) return 0

        // Mapeia as faces para valores numéricos (A=14, K=13, Q=12, J=11, 10=10, 9=9)
        val faceValues =
            mapOf(
                "A" to 14,
                "K" to 13,
                "Q" to 12,
                "J" to 11,
                "10" to 10,
                "9" to 9,
            )

        val diceValues = dice.mapNotNull { faceValues[it] }
        if (diceValues.size != 5) return 0

        // Conta a frequência de cada face
        val frequency = dice.groupingBy { it }.eachCount()
        val counts = frequency.values.sortedDescending()

        return when {
            // Five of a Kind (5 iguais) - ex: A,A,A,A,A
            counts[0] == 5 -> {
                val face = frequency.keys.first()
                1000 + (faceValues[face] ?: 0) * 100
            }

            // Four of a Kind (4 iguais) - ex: K,K,K,K,9
            counts[0] == 4 -> {
                val fourFace = frequency.filter { it.value == 4 }.keys.first()
                800 + (faceValues[fourFace] ?: 0) * 10
            }

            // Full House (3 iguais + 2 iguais) - ex: Q,Q,Q,J,J
            counts[0] == 3 && counts[1] == 2 -> {
                val threeKind = frequency.filter { it.value == 3 }.keys.first()
                val pair = frequency.filter { it.value == 2 }.keys.first()
                600 + (faceValues[threeKind] ?: 0) * 10 + (faceValues[pair] ?: 0)
            }

            // Straight (sequência) - ex: A,K,Q,J,10 ou K,Q,J,10,9
            diceValues.sorted() == listOf(9, 10, 11, 12, 13) ||
                diceValues.sorted() == listOf(10, 11, 12, 13, 14) -> {
                500 + diceValues.max()
            }

            // Three of a Kind (3 iguais) - ex: J,J,J,Q,9
            counts[0] == 3 -> {
                val threeFace = frequency.filter { it.value == 3 }.keys.first()
                400 + (faceValues[threeFace] ?: 0) * 10
            }

            // Two Pair (2 pares) - ex: Q,Q,10,10,9
            counts[0] == 2 && counts[1] == 2 -> {
                val pairs =
                    frequency
                        .filter { it.value == 2 }
                        .keys
                        .sortedByDescending { faceValues[it] }
                200 + (faceValues[pairs[0]] ?: 0) * 10 + (faceValues[pairs[1]] ?: 0)
            }

            // One Pair (1 par) - ex: K,K,Q,J,9
            counts[0] == 2 -> {
                val pairFace = frequency.filter { it.value == 2 }.keys.first()
                100 + (faceValues[pairFace] ?: 0) * 10
            }

            // High Card (nada) - apenas a face mais alta
            else -> diceValues.max()
        }
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
// TODO ("TMB PODE COMEÇAR O JOGO QUANDO PASSAR O TEMPO E JA TIVER O N MIN)
}
