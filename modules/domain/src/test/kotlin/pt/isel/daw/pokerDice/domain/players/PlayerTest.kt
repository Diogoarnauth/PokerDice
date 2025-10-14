package pt.isel.daw.pokerDice.domain.players

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pt.isel.daw.pokerDice.domain.users.PasswordValidationInfo
import pt.isel.daw.pokerDice.domain.users.User

class PlayerTest {
    private fun createPlayer(
        id: Int,
        name: String = "Player$id",
    ) = User(
        id = id,
        username = name.lowercase(),
        passwordValidation = PasswordValidationInfo("val"),
        name = name,
        age = 25,
        credit = 100,
        winCounter = 0,
    )

    @Test
    fun `deve criar player válido`() {
        val p = createPlayer(1, "Renata")

        assertEquals("renata", p.username)
        // assertTrue(p.token is UUID)
        assertEquals(100, p.credit)
    }

    @Test
    fun `deve lançar erro se idade for inválida`() {
        val ex =
            assertThrows(IllegalArgumentException::class.java) {
                User(
                    id = 1,
                    username = "renata",
                    passwordValidation = PasswordValidationInfo("val"),
                    name = "Renata",
                    age = 17,
                    credit = 100,
                    winCounter = 0,
                )
            }
        assertTrue(ex.message!!.contains("Age must be between 18 and 100"))
    }

    @Test
    fun `incrementCredit deve adicionar créditos corretamente`() {
        val p =
            User(
                id = 1,
                username = "renata",
                passwordValidation = PasswordValidationInfo("val"),
                name = "Renata",
                age = 25,
                credit = 10,
                winCounter = 0,
            )

        p.incrementCredit(15)
        assertEquals(25, p.credit)
    }

    @Test
    fun `winCounter deve incrementar corretamente`() {
        val p =
            User(
                id = 1,
                username = "renata",
                passwordValidation = PasswordValidationInfo("val"),
                name = "Renata",
                age = 25,
                credit = 100,
                winCounter = 2,
            )

        p.winCounter()
        assertEquals(3, p.winCounter)
    }
}
