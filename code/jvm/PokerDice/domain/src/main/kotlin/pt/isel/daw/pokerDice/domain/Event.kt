package pt.isel.daw.pokerDice.domain

import kotlinx.datetime.Instant

sealed interface Event { // TODO()
    data class TODO(
        val id: Long,
        val gelado: String,
    ) : Event

    data class KeepAlive(
        val timestamp: Instant,
    ) : Event
}
