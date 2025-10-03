package org.example.Domain.Games

import org.example.Domain.Players.Player

class Round (
    val id: Int,
    var Winner: Player? = null,
    var Bet: Int,
    var roundOver : Boolean = false,
    val timeToPlay : Int //possivelmente depois alterar para estrutura de tempo
){
    init {
        require(id > 0) { "ID must be greater than zero." }
        require(Winner == null){"A new round can´t start with an winner defined"}
        require(!roundOver) { "ID must be greater than zero." }
        require(Bet >= 10){"Bets must be bigger than 10 credits"}
        require(timeToPlay >= 1000){"There is a minimum of 1 minute to play"}
    }
    fun defineWinner(winner : Player){
        this.Winner = winner
    }
    fun defineBet(bet: Int){ //ns se precisamos
        Bet = bet
    }
    fun endRound(){roundOver = true}
}


