package pt.isel.daw.pokerDice.domain.games

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pt.isel.daw.pokerDice.domain.players.PasswordValidationInfo
import pt.isel.daw.pokerDice.domain.players.Player
import kotlin.time.Duration.Companion.minutes
import java.util.*

class GameLogicTest {

    private lateinit var logic: GameLogic
    private val fakeClock = object : Clock {
        override fun now(): Instant = Instant.parse("2025-01-01T00:00:00Z")
    }

    private fun createPlayer(id: Int, name: String = "Player$id") = Player(
        id = id,
        username = name.lowercase(),
        password = "1234",
        passwordValidation = PasswordValidationInfo("val"),
        name = name,
        age = 25,
        credit = 100,
        winCounter = 0
    )

    @BeforeEach
    fun setup() {
        logic = GameLogic(fakeClock, 10.minutes)
    }

    @Test
    fun `createNewGame deve criar um jogo válido e vazio`() {
        val game = logic.createNewGame(nrPlayers = 2, minCredits = 50)

        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.state)
        assertTrue(game.players.isEmpty())
        assertEquals(2, game.nrPlayers)
        assertEquals(50, game.minCredits)
    }

    @Test
    fun `addPlayer deve adicionar jogador válido quando o jogo está à espera`() {
        val game = logic.createNewGame(2, 50)
        val player = createPlayer(1, "Alice")

        val result = logic.addPlayer(game, playerCredits = 100, newPlayer = player)
        val updatedGame = (result as GameResult.WaitingForMorePlayers).game

        assertEquals(1, updatedGame.players.size)
        assertEquals(Game.State.WAITING_FOR_PLAYERS, updatedGame.state)
        assertEquals("Alice", updatedGame.players[0].name)
    }

    @Test
    fun `addPlayer deve começar o jogo quando chega o número total de jogadores`() {
        val game = logic.createNewGame(2, 10)
        val p1 = createPlayer(1, "Alice")
        val p2 = createPlayer(2, "Bob")

        val g1 = (logic.addPlayer(game, 100, p1) as GameResult.WaitingForMorePlayers).game
        val result = logic.addPlayer(g1, 100, p2)

        assertTrue(result is GameResult.GameStarted)
        val updatedGame = (result as GameResult.GameStarted).game
        assertEquals(Game.State.RUNNING, updatedGame.state)
        assertEquals(2, updatedGame.players.size)
    }

    @Test
    fun `addPlayer deve falhar se o jogador já existir`() {
        val game = logic.createNewGame(2, 10)
        val p1 = createPlayer(1, "Alice")

        val g1 = (logic.addPlayer(game, 100, p1) as GameResult.WaitingForMorePlayers).game
        val result = logic.addPlayer(g1, 100, p1)

        assertEquals(GameResult.PlayerAlreadyExistInGame, result)
    }

    @Test
    fun `addPlayer deve falhar se não houver vagas`() {
        val game = logic.createNewGame(1, 10)
        val p1 = createPlayer(1, "Alice")
        val p2 = createPlayer(2, "Bob")

        val g1 = (logic.addPlayer(game, 100, p1) as GameResult.GameStarted).game
        val result = logic.addPlayer(g1, 100, p2)

        assertEquals(GameResult.InvalidState(" It is not possible to add players right now."), result)
    }


    @Test
    fun `applyRound deve lançar erro se o jogador não estiver na lista`() {
        val game = logic.createNewGame(2, 10).copy(state = Game.State.NEXT_PLAYER)
        val p1 = Player(1,
                        UUID.randomUUID(),
                        "renataa",
                        "renataa",
                        PasswordValidationInfo("val"),
                        "Renata",
                        20,
                        50,
                        2)

        val result = logic.applyRound(game, p1)
        assertEquals(GameResult.NotAPlayer, result)
    }



    @Test
    fun `determineCombination deve identificar corretamente as combinações`() {
        fun roll(vararg values: Int) =
            values.map { v -> Dice.faces.first { it.value == v } }

        assertEquals(CombinationType.FIVE_OF_A_KIND, logicTestCombination(roll(9, 9, 9, 9, 9)))
        assertEquals(CombinationType.FOUR_OF_A_KIND, logicTestCombination(roll(10, 10, 10, 10, 9)))
        assertEquals(CombinationType.FULL_HOUSE, logicTestCombination(roll(11, 11, 11, 12, 12)))
        assertEquals(CombinationType.THREE_OF_A_KIND, logicTestCombination(roll(13, 13, 13, 9, 10)))
        assertEquals(CombinationType.TWO_PAIR, logicTestCombination(roll(14, 14, 12, 12, 10)))
        assertEquals(CombinationType.ONE_PAIR, logicTestCombination(roll(9, 9, 10, 11, 12)))
        assertEquals(CombinationType.HIGH_CARD, logicTestCombination(roll(9, 10, 11, 12, 13)))
    }

    // helper para testar a função privada determineCombination
    private fun logicTestCombination(roll: List<Dice>): CombinationType {
        val method = GameLogic::class.java.getDeclaredMethod("determineCombination", List::class.java)
        method.isAccessible = true
        return method.invoke(logic, roll) as CombinationType
    }
}