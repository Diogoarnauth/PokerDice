package pt.isel.daw.pokerDice.domain.users

import java.util.UUID

data class User(
    val id: Int,
    val token: UUID = UUID.randomUUID(),
    val username: String,
    val passwordValidation: PasswordValidationInfo,
    // dúvidas, qual a diferença entre ambas
    val name: String,
    val age: Int,
    var credit: Int,
    var winCounter: Int,
    var lobbyId: Int? = null,
) {
    init {
        // Validações usando 'require', ver depois como se lança os erros
        require(id > 0) { "ID must be greater than zero." }
        require(username.isNotBlank()) { "Username cannot be blank." }
        require(name.isNotBlank()) { "Name cannot be blank." }
        require(age in 18..100) { "Age must be between 18 and 100." }
        require(credit >= 0) { "Credit cannot be negative." }
        require(winCounter >= 0) { "WinCounter cannot be negative." }
    }

    fun incrementCredit(newCredits: Int) {
        credit += newCredits
    }

    fun winCounter() {
        winCounter += 1
    }
}
