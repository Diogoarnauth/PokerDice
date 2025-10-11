package pt.isel.daw.pokerDice.http.model

import javax.print.attribute.standard.RequestingUserName

class PlayerCreateInputModel (
    val username: String,
    val name: String,
    val age: Int,
    val password: String,
    val inviteCode: String,
)
