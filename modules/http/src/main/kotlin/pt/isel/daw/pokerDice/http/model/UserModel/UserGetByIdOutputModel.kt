package pt.isel.daw.pokerDice.http.model.UserModel
import java.util.UUID

data class UserGetByIdOutputModel(
    val username: String,
    val token: UUID,
    val name: String
)