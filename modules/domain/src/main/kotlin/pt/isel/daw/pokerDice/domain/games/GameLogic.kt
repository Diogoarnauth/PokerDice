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
            gameWinner = null,
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
            gameWinner = null,
            nrUsers = nrUsers,
        )
}

/*
    fun addPlayer(
        lobby: Lobby,
        playerCredits: Int,
        newPlayer: User,
    ): GameResult {
        if (lobby.isRunning) {
            return GameResult.InvalidState(" It is not possible to add players right now.")
        }

        if (game.minCredits > playerCredits) {
            return GameResult.NotEnoughCredits("It is necessary more credits to play this game.", game.minCredits)
        }

        if (game.players.any { it.id == newPlayer.id }) {
            return GameResult.PlayerAlreadyExistInGame
        }

        if (game.players.size >= game.nrPlayers) {
            return GameResult.GameFull
        }

        // guardar temporariamente o número de jogadores atuais
        val updatedPlayers = game.players + newPlayer
        val updatedGame =
            if (updatedPlayers.size == game.nrPlayers) {
                // todos os jogadores estão prontos -> o jogo começa
                game.copy(
                    // n pode ter game.copy TODO(" ")
                    players = updatedPlayers,
                    state = Game.State.RUNNING,
                )
            } else {
                game.copy(players = updatedPlayers)
            }

        return if (updatedGame.state == Game.State.RUNNING) {
            GameResult.GameStarted(updatedGame)
        } else {
            GameResult.WaitingForMorePlayers(updatedGame)
        }
    }






    /**
 * Aplica uma jogada (round). Para já, apenas faz validações básicas.
 * Mais tarde, vai ser expandido com a lógica do lançamento de dados.
 */
    fun applyRound(
        game: Game,
        player: User,
    ): GameResult {
        // 1. Verificar se o jogador pertence ao jogo
        if (player.id !in game.players.map { it.id }) {
            return GameResult.NotAPlayer
        }

        // 2. Verificar se o estado é aceite para jogar
        if (!game.state.isRunning || game.state != Game.State.NEXT_PLAYER) {
            return GameResult.InvalidState(" The game hasn't started, or it's already ended.")
        }

        // 3. Verificar se é a vez deste jogador
        val currentIndex = game.currentPlayerIndex
        if (currentIndex >= game.players.size) {
            return GameResult.InvalidState("Invalid current player index.")
        }
        val currentPlayer = game.players[currentIndex]
        if (player.id != currentPlayer.id) {
            return GameResult.InvalidState("It is not your turn.")
        }

        // 4. Preparar dados de timestamp
        val now = clock.now()
        val diceRolls = List(5) { Dice.random() }

        // 5. Determinar a combinação e pontuação
        // val combination = determineCombination(diceRolls)
        val score = PokerDiceScorer.score(diceRolls)

        // 6. Atualizar/obter ronda atual
        val updateRounds = game.rounds.toMutableList()
        val lastRound = updateRounds.lastOrNull()
        val currentRound: Round =
            if (lastRound == null || lastRound.roundOver) {
                // criar nova ronda
                val r =
                    Round(
                        id = updateRounds.size + 1,
                        bet = 10,
                        timeToPlay = 60_000,
                    )

                updateRounds.add(r)
                r
            } else {
                lastRound
            }

        // 7. Atualizar pontuações globais (por player.id)
        val play =
            Play(
                playerId = player.id,
                dice = diceRolls,
                score = score,
                timestamp = now,
            )
        currentRound.addPlay(play)

        // 8. Atualizar pontuações globais (por player.id)
        val updatedScores = game.scores.toMutableMap()
        updatedScores[player] = updatedScores.getOrDefault(player, 0) + score

        // 9. Verificar se todos os players já jogaram esta ronda
        val allPlayed =
            currentRound.plays
                .map { it.playerId }
                .distinct()
                .size == game.players.size

        // 10. Se todos jogaram -> decidir vencedor desta ronda
        if (allPlayed) {
            val winner = determineRoundWinner(currentRound, game.players)
            if (winner != null) {
                currentRound.defineWinner(winner)
                winner.winCounter() // atualiza o contador interno do Player
            }
            currentRound.endRound()
        }

        // 11. Atualiza o índice / estado do jogo
        val isLastPlayer = currentIndex == game.currentPlayerIndex
        val nextState: Game.State
        val nextIndex: Int
        if (allPlayed) {
            if (isLastPlayer) {
                nextState = Game.State.ENDED
                nextIndex = 0
            } else {
                nextState = Game.State.NEXT_PLAYER
                nextIndex = (currentIndex + 1) % game.players.size
            }
        } else {
            // ronda não completa: apenas avança para o próximo jogador
            nextState = Game.State.NEXT_PLAYER
            nextIndex = (currentIndex + 1) % game.players.size
        }

        // 12. Construir um Game atualizado
        val updatedGame =
            game.copy(
                rounds = updateRounds,
                scores = updatedScores,
                currentPlayerIndex = nextIndex,
                state = nextState,
            )

        // 13. Resultado correto
        return if (nextState == Game.State.ENDED) {
            GameResult.GameEnded(updatedGame, now)
        } else {
            GameResult.RoundApplied(updatedGame, now)
        }
    }

    fun reshufle() {
    }

    fun determineRoundWinner(
        round: Round,
        players: List<User>,
    ): User? {
        val bestScore = round.plays.maxOfOrNull { it.score } ?: return null
        val candidates = round.plays.filter { it.score == bestScore }

        if (candidates.size == 1) {
            return players.first { it.id == candidates[0].playerId }
        }

        // tie-breaker: maior face individual
        val bestByHighCard = candidates.maxByOrNull { it.dice.maxOf { d -> d.value } }!!
        return players.first { it.id == bestByHighCard.playerId }
    }

    /**
 * Determina o tipo de combinação obtida.
 */
    private fun determineCombination(roll: List<Dice>): CombinationType {
        val counts =
            roll
                .groupingBy { it.value }
                .eachCount()
                .values
                .sortedDescending()
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

*/
