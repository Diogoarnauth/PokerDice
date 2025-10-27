package pt.isel.daw.pokerDice.http.model.game

data class GameStateOutputModel(
    val gameId: String,
    val lobbyId: Int,
    val status: String,
    // e.g. “IN_PROGRESS”, “FINISHED”
    val currentRound: RoundInfo?,
    val players: List<PlayerState>,
)

data class RoundInfo(
    val roundNumber: Int,
    val turnPlayerId: Int,
    val rollCount: Int,
    val dice: List<Int>,
// valores atuais dos dados do jogador em turno
)

data class PlayerState(
    val playerId: Int,
    val credit: Int,
    // opcional: a mão final da ronda anterior, etc.
    val lastHand: String? = null,
)
// TODO("VERIFICAR SE É PRECISO ADICIONAR MAIS INFORMAÇÃO AOS MODELOS")
