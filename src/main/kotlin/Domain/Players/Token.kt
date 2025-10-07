
import kotlinx.datetime.Instant

class Token(
    val tokenValidationInfo: TokenValidationInfo,
    val playerId: Int,
    val createdAt: Instant,
    val lastUsedAt: Instant,
)
