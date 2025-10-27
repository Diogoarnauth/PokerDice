package pt.isel.daw.pokerDice.http.model.userModel

class UserCreateInputModel(
    val username: String,
    val name: String,
    val age: Int,
    val password: String,
    val inviteCode: String,
)
