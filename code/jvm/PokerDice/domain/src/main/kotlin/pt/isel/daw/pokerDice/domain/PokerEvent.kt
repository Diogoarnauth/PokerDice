package pt.isel.daw.pokerDice.domain

import kotlinx.datetime.Instant

sealed interface PokerEvent {
    val type: String

    data class LobbiesListChanges(
        val lobbyId: Int,
        val name: String?,
        val hostUsername: String?,
        val changeType: String,
    ) : PokerEvent {
        override val type = "lobbies_list_changes"
    }

    data class GameStarted(
        val lobbyId: Int,
        val gameId: Int,
        val name: String?,
        val hostUsername: String?,
        val changeType: String = "started",
    ) : PokerEvent {
        override val type = "gameStarted"
    }

    data class GameUpdated(
        val lobbyId: Int,
        val changeType: String?,
    ) : PokerEvent {
        override val type = "gameUpdated"
    }

    data class WinnerAlert(
        val lobbyId: Int,
        val winners: String?,
        val changeType: String?,
    ) : PokerEvent {
        override val type = "winnerAlert"
    }

    data class PlayerJoined(
        val lobbyId: Int,
        val username: String,
    ) : PokerEvent {
        override val type = "player_joined"
    }

    data class PlayerLeft(
        val lobbyId: Int,
        val username: String,
    ) : PokerEvent {
        override val type = "player_left"
    }

    data class RoundEnded(
        val round: Int,
        val winners: List<String>,
    ) : PokerEvent {
        override val type = "round_ended"
    }

    data class GameEnded(
        val winners: List<String>,
    ) : PokerEvent {
        override val type = "game_ended"
    }

    data class KeepAlive(
        val timestamp: Instant,
    ) : PokerEvent {
        override val type = "keepalive"
    }
}
