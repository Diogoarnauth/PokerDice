package org.example.HTTP.model

import org.example.Domain.Players.Player

class LobbyCreateInputModel (
    val owner: Player, //trocar para authenticated player
    val name: String,
    val description: String,
    val minPlayers : Int,
    val maxPlayers : Int,
    val nRounds : Int
)
