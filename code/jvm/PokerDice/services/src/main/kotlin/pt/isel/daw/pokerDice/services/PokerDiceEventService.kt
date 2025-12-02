package pt.isel.daw.pokerDice.services

import jakarta.inject.Named
import org.slf4j.LoggerFactory
import pt.isel.daw.pokerDice.domain.EventEmitter
import pt.isel.daw.pokerDice.domain.PokerEvent
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

data class ListenerInfo(
    val emitter: EventEmitter,
    val userId: Int,
)

@Named
class PokerDiceEventService {
    companion object {
        private val logger = LoggerFactory.getLogger(PokerDiceEventService::class.java)
    }

    private val listeners = mutableListOf<ListenerInfo>()
    private val lock = ReentrantLock()

    private val scheduler: ScheduledExecutorService =
        Executors.newScheduledThreadPool(1).also {
            it.scheduleAtFixedRate({ keepAlive() }, 2, 2, TimeUnit.SECONDS)
        }

    fun addListener(
        userId: Int,
        emitter: EventEmitter,
    ) = lock.withLock {
        logger.info("Added listener for user $userId")

        listeners.add(ListenerInfo(emitter, userId))

        emitter.onCompletion {
            removeListener(emitter)
        }
        emitter.onError {
            removeListener(emitter)
        }
    }

    fun removeListener(emitter: EventEmitter) =
        lock.withLock {
            listeners.removeIf { it.emitter == emitter }
        }

    fun sendToUser(
        userId: Int,
        event: PokerEvent,
    ) = lock.withLock {
        listeners.find { it.userId == userId }?.emitter?.emit(event)
    }

    fun sendToLobby(
        userIds: List<Int>,
        event: PokerEvent,
    ) = lock.withLock {
        userIds.forEach { id ->
            listeners.find { it.userId == id }?.emitter?.emit(event)
        }
    }

    fun sendToAll(event: PokerEvent) =
        lock.withLock {
            listeners.forEach {
                try {
                    it.emitter.emit(event)
                } catch (e: Exception) {
                    logger.info("Error while sending SSE: ${e.message}")
                }
            }
        }

    private fun keepAlive() =
        lock.withLock {
            val event = PokerEvent.KeepAlive
            listeners.forEach {
                try {
                    it.emitter.emit(event)
                } catch (_: Exception) {
                }
            }
        }
}
