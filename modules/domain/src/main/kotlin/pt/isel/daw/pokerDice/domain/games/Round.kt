package pt.isel.daw.pokerDice.domain.games

// Round.kt
data class Round(
    val id: Int?,
    val gameId: Int,
    val roundWinners: Int? = null,
    var roundNumber: Int,
    var bet: Int?,
    // null caso ele desista de jogar o round
    // turno ou id ?
    var roundOver: Boolean,
) {
    // TODO("")

    /*
    init {
        require(id > 0) { "ID must be greater than zero." }
        require(winner == null) { "A new round can´t start with an winner defined" }
        require(!roundOver) { "ID must be greater than zero." }
        require(bet >= 10) { "Bets must be bigger than 10 credits" }
        require(timeToPlay >= 1000) { "There is a minimum of 1 minute to play" }
    }

    fun addPlay(play: Play) {
        startTime = play.timestamp
        plays.add(play)
    }

    fun defineWinner(winner: User) {
        this.winner = winner
    }

    fun defineBet(amount: Int) { // ns se precisamos
        require(amount >= 10) { "Bet must be at least 10 credits." }
        this.bet = bet
    }

    fun endRound() {
        roundOver = true
    }*/
}
