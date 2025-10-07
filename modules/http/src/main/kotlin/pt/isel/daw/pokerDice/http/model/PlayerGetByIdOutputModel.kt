import java.util.UUID

data class PlayerGetByIdOutputModel(
    val username: String,
    val token: UUID,
    val name: String
)