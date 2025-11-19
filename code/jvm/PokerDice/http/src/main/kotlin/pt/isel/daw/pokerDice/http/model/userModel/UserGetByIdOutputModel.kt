package pt.isel.daw.pokerDice.http.model.userModel

data class UserGetByIdOutputModel(
    val username: String,
    val name: String,
    val age: Int,
    val lobby: Int?,
    val credits: Int,
    val winCounts: Int,
)
