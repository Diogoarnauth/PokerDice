package pt.isel.daw.pokerDice.domain.players

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class PlayerTest {

    private fun createPlayer(id: Int, name: String = "Player$id") = Player(
        id = id,
        username = name.lowercase(),
        passwordValidation = PasswordValidationInfo("val"),
        name = name,
        age = 25,
        credit = 100,
        winCounter = 0
    )

    @Test
    fun `deve criar player válido`() {
        val p = createPlayer(1, "Renata")

        assertEquals("renata", p.username)
        //assertTrue(p.token is UUID)
        assertEquals(100, p.credit)
    }

    @Test
    fun `deve lançar erro se idade for inválida`() {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            Player(
                id = 1,
                username = "renata",
                passwordValidation = PasswordValidationInfo("val"),
                name = "Renata",
                age = 17,
                credit = 100,
                winCounter = 0
            )
        }
        assertTrue(ex.message!!.contains("Age must be between 18 and 100"))
    }

    @Test
    fun `incrementCredit deve adicionar créditos corretamente`() {
        val p = Player(
            id = 1,
            username = "renata",
            passwordValidation = PasswordValidationInfo("val"),
            name = "Renata",
            age = 25,
            credit = 10,
            winCounter = 0
        )

        p.incrementCredit(15)
        assertEquals(25, p.credit)
    }

    @Test
    fun `winCounter deve incrementar corretamente`() {
        val p = Player(
            id = 1,
            username = "renata",
            passwordValidation = PasswordValidationInfo("val"),
            name = "Renata",
            age = 25,
            credit = 100,
            winCounter = 2
        )

        p.winCounter()
        assertEquals(3, p.winCounter)
    }
}