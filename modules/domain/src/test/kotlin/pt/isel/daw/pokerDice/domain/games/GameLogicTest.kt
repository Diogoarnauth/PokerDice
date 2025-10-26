package pt.isel.daw.pokerDice.domain.games

import kotlinx.datetime.Clock
import pt.isel.daw.pokerDice.domain.users.PasswordValidationInfo
import pt.isel.daw.pokerDice.domain.users.User
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.time.Duration.Companion.minutes
/*
class GameLogicTests {

    private val clock = Clock.System
    private val gameLogic = GameLogic(clock, 1.minutes)

    private val alice =
        User(1, "alice", PasswordValidationInfo(""), "Alice", 25, credit = 100, winCounter = 0)
    private val bob =
        User(2, "bob", PasswordValidationInfo(""), "Bob", 30, credit = 100, winCounter = 0)
    private val charlie =
        User(3, "charlie", PasswordValidationInfo(""), "Charlie", 22, credit = 100, winCounter = 0)

    // -----------------------------------------------------
    // TEST 1: Criar novo jogo
    // -----------------------------------------------------
    @Test
    fun `create new game starts waiting for players`() {
        val game = gameLogic.createNewGame(nrPlayers = 2, minCredits = 50, lobbyId = 1)
        assertEquals(Game.State.WAITING_FOR_PLAYERS, game.state)
        assertEquals(0, game.players.size)
        assertEquals(2, game.nrPlayers)
    }

    // -----------------------------------------------------
    // TEST 2: Adicionar jogadores
    // -----------------------------------------------------
    @Test
    fun `add player until game starts`() {
        var result = gameLogic.addPlayer(
            gameLogic.createNewGame(2, 10, 1),
            alice.credit,
            alice
        )

        val waitingGame =
            when (result) {
                is GameResult.WaitingForMorePlayers -> result.game
                else -> fail("Unexpected result $result")
            }
        assertEquals(1, waitingGame.players.size)
        assertEquals(Game.State.WAITING_FOR_PLAYERS, waitingGame.state)

        // segundo jogador inicia o jogo
        result = gameLogic.addPlayer(waitingGame, bob.credit, bob)
        val runningGame =
            when (result) {
                is GameResult.GameStarted -> result.game
                else -> fail("Unexpected result $result")
            }
        assertEquals(2, runningGame.players.size)
        assertEquals(Game.State.RUNNING, runningGame.state)
    }

    // -----------------------------------------------------
    // TEST 3: Não pode entrar sem créditos
    // -----------------------------------------------------
    @Test
    fun `cannot join game without enough credits`() {
        val game = gameLogic.createNewGame(2, minCredits = 200, lobbyId = 1)
        val result = gameLogic.addPlayer(game, playerCredits = 100, newPlayer = alice)
        assertTrue(result is GameResult.NotEnoughCredits)
    }

    // -----------------------------------------------------
    // TEST 4: Não pode entrar se o jogo estiver cheio
    // -----------------------------------------------------
    @Test
    fun `cannot join full game`() {
        var result = gameLogic.addPlayer(
            gameLogic.createNewGame(2, 10, 1),
            alice.credit,
            alice
        )
        val g1 =
            when (result) {
                is GameResult.WaitingForMorePlayers -> result.game
                else -> fail("Unexpected $result")
            }

        result = gameLogic.addPlayer(g1, bob.credit, bob)
        val fullGame =
            when (result) {
                is GameResult.GameStarted -> result.game
                else -> fail("Unexpected $result")
            }

        val tooMany = gameLogic.addPlayer(fullGame, charlie.credit, charlie)
        assertTrue(tooMany is GameResult.GameFull)
    }

    // -----------------------------------------------------
    // TEST 5: Não pode entrar duas vezes
    // -----------------------------------------------------
    @Test
    fun `player cannot join twice`() {
        var game = gameLogic.createNewGame(2, 10, 1)
        var result = gameLogic.addPlayer(game, alice.credit, alice)
        game =
            when (result) {
                is GameResult.WaitingForMorePlayers -> result.game
                else -> fail("Unexpected $result")
            }

        // tenta entrar de novo
        result = gameLogic.addPlayer(game, alice.credit, alice)
        assertTrue(result is GameResult.PlayerAlreadyExistInGame)
    }

    // -----------------------------------------------------
    // TEST 6: Jogador não pode jogar se não estiver no jogo
    // -----------------------------------------------------
    @Test
    fun `cannot play if not a player`() {
        val game = gameLogic.createNewGame(2, 10, 1)
        val outsider = charlie
        val result = gameLogic.applyRound(game, outsider)
        assertTrue(result is GameResult.NotAPlayer)
    }
/*
    // -----------------------------------------------------
    // TEST 7: Jogadas válidas e jogo termina
    // -----------------------------------------------------
    @Test
    fun `apply rounds until game ends`() {
        var result = gameLogic.addPlayer(
            gameLogic.createNewGame(2, 10, 1),
            alice.credit,
            alice
        )
        var game =
            when (result) {
                is GameResult.WaitingForMorePlayers -> result.game
                else -> fail("Unexpected $result")
            }

        result = gameLogic.addPlayer(game, bob.credit, bob)
        game =
            when (result) {
                is GameResult.GameStarted -> result.game
                else -> fail("Unexpected $result")
            }

        // Jogadas alternadas até terminar
        var playResult = gameLogic.applyRound(game, alice)
        game =
            when (playResult) {
                is GameResult.RoundApplied -> playResult.game
                is GameResult.GameEnded -> playResult.game
                else -> fail("Unexpected $playResult")
            }

        playResult = gameLogic.applyRound(game, bob)
        when (playResult) {
            is GameResult.RoundApplied,
            is GameResult.GameEnded -> assertTrue(true)
            else -> fail("Unexpected $playResult")
        }
    }*/
}
*/