package pt.isel.daw.pokerDice.domain.users

class AuthenticatedUser(
    val user: User,
    val token: String,
)
