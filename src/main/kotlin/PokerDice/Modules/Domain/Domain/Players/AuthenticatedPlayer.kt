package org.example.PokerDice.Modules.Domain.Domain.Players

import org.example.Domain.Players.Player


class AuthenticatedPlayer(
    val player: Player,
    val token: String,
)
