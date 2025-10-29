package pt.isel.daw.pokerDice.domain.games

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.random.Random

class DiceTest {
    @Test
    fun `faces deve conter 6 valores únicos`() {
        assertEquals(6, Dice.faces.size)
        assertEquals(setOf(9, 10, 11, 12, 13, 14), Dice.faces.map { it.value }.toSet())
    }

    @Test
    fun `random deve devolver uma das faces válidas`() {
        repeat(100) {
            val face = Dice.random(Random(it))
            assertTrue(face in Dice.faces)
        }
    }

    @Test
    fun `from deve devolver a face correta`() {
        assertEquals(Dice.Nine, Dice.from("nine"))
        assertEquals(Dice.King, Dice.from("KING"))
    }

    @Test
    fun `from deve lançar erro para label inválido`() {
        val ex =
            assertThrows(IllegalStateException::class.java) {
                Dice.from("joker")
            }
        assertTrue(ex.message!!.contains("Invalid face"))
    }
}
