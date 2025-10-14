package pt.isel.daw.pokerDice.http.model.userModel
import java.util.UUID

data class UserGetByIdOutputModel(
    val username: String,
    val token: UUID,
    val name: String,
)
