package pt.isel.daw.pokerDice.domain.users

data class User(
    val id: Int,
    val username: String,
    val passwordValidation: PasswordValidationInfo,
    val name: String,
    val age: Int,
    var credit: Int,
    var winCounter: Int,
    var lobbyId: Int? = null,
)
