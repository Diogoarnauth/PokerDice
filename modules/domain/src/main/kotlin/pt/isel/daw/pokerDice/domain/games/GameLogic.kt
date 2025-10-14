package pt.isel.daw.pokerDice.domain.games

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import pt.isel.daw.pokerDice.domain.users.User
import java.util.UUID
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

object PokerDiceScorer {
    fun score(dice: List<Dice>): Int {
        require(dice.size == 5) { "A roll must contain exactly 5 dice" }

        val counts = dice.groupingBy { it.value }.eachCount()
        // val distinct = counts.size

        return when {
            counts.values.contains(5) -> 900
            counts.values.contains(4) -> 800
            counts.values.sorted() == listOf(2, 3) -> 700
            isStraight(dice) -> 600
            counts.values.contains(3) -> 500
            counts.values.count { it == 2 } == 2 -> 400
            counts.values.contains(2) -> 300
            else -> 200 + dice.maxOf { it.value }
            // high card bonus
        }
    }

    private fun isStraight(dice: List<Dice>): Boolean {
        val sorted = dice.map { it.value }.sorted()
        return sorted.zipWithNext().all { (a, b) -> b - a == 1 }
    }
}

class GameLogic(
    private val clock: Clock,
    private val duration: Duration,
) {
    /**
     * Cria um novo game ainda sem jogadores
     */
    fun createNewGame(
        nrPlayers: Int,
        minCredits: Int,
        lobbyId: UUID,
    ): Game =
        Game(
            id = UUID.randomUUID(),
            lobbyId = lobbyId,
            state = Game.State.WAITING_FOR_PLAYERS,
            nrPlayers = nrPlayers,
            minCredits = minCredits,
            players = emptyList(),
            currentPlayerIndex = 0,
            lastRoll = emptyList(),
            lastCombination = null,
        )

    /**
     * Adiciona, se possível, um novo jogador ao jogo.
     */
    fun addPlayer(
        game: Game,
        playerCredits: Int,
        newPlayer: User,
    ): GameResult {
        if (game.state != Game.State.WAITING_FOR_PLAYERS) {
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
                    // n pode ter game.copy
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
