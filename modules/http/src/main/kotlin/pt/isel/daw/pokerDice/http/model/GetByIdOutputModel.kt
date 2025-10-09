package pt.isel.daw.pokerDice.http.model

class GetByIdOutputModel (
        val id: Int,
        val username: String,
        val name : String,
        val age : Int,
        var credit : Int,
        var winCounter : Int
    )
