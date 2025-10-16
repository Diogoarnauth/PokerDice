package pt.isel.daw.pokerDice.domain.users
import java.time.Instant

class Token(
    val tokenValidationInfo: TokenValidationInfo,
    val userId: Int,
    val createdAt: Instant,
    val lastUsedAt: Instant,
)
