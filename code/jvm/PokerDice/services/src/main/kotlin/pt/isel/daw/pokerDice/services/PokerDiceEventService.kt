package pt.isel.daw.pokerDice.services

import jakarta.inject.Named
import kotlinx.datetime.Clock
import org.slf4j.LoggerFactory
import pt.isel.daw.pokerDice.domain.EventEmitter
import pt.isel.daw.pokerDice.domain.PokerEvent
import pt.isel.daw.pokerDice.domain.Topic
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

data class ListenerInfo(
    val emitter: EventEmitter,
    val userId: Int,
    val topic: Topic,
)

@Named
class PokerDiceEventService {
    companion object {
        private val logger = LoggerFactory.getLogger(PokerDiceEventService::class.java)
    }

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val listeners = mutableListOf<ListenerInfo>()
    private val lock = ReentrantLock()
    var counter = 0

    private val scheduler: ScheduledExecutorService =
        Executors.newScheduledThreadPool(1).also {
            it.scheduleAtFixedRate({ keepAlive() }, 2, 2, TimeUnit.SECONDS)
        }

    fun shutdown() {
        logger.info("shutting down event scheduler")
        scheduler.shutdown()
    }

    /**
     * Register a new SSE listener for an authenticated user and a specific Topic.
     */
    fun addEventEmitter(
        userId: Int,
        topic: Topic,
        listener: EventEmitter,
    ) = lock.withLock {
        logger.info("Added listener for user $userId on topic '${topic.value}'")

        listeners.add(ListenerInfo(listener, userId, topic))

        listener.onCompletion { removeListener(listener) }
        listener.onError { removeListener(listener) }

        listener
    }

    /**
     * Send event to all listeners that are subscribed to a specific Topic.
     */
    fun sendToTopic(
        topic: Topic,
        event: PokerEvent,
    ) = lock.withLock {
        listeners
            .filter { it.topic.value == topic.value }
            .forEach {
                try {
                    it.emitter.emit(event)
                } catch (e: Exception) {
                    logger.warn("Error sending event to topic '${topic.value}': ${e.message}")
                }
            }
    }

    fun getListener(userId: Int) =
        lock.withLock {
            listeners.find { it.userId == userId }?.emitter
        }

    fun logout(listener: EventEmitter) =
        lock.withLock {
            listener.complete()
        }

    /**
     * Send event to a single user, no matter what topic they're listening to.
     */
    fun sendToUser(
        userId: Int,
        event: PokerEvent,
    ) = lock.withLock {
        listeners
            .filter { it.userId == userId }
            .forEach {
                try {
                    it.emitter.emit(event)
                } catch (_: Exception) {
                }
            }
    }

    /**
     * Send event to every connected listener (rarely useful now).
     */
    fun sendToAll(event: PokerEvent) =
        lock.withLock {
            logger.info("listeners $listeners")
            listeners.forEach {
                try {
                    logger.info("COUNTER $counter")
                    counter += 1

                    it.emitter.emit(event)
                } catch (_: Exception) {
                }
            }
        }

    fun removeListener(emitter: EventEmitter) =
        lock.withLock {
            listeners.removeIf { it.emitter == emitter }
        }

    /**
     * Keep SSE connection alive with periodic pings.
     */
    private fun keepAlive() =
        lock.withLock {
            val keepAliveEvent = PokerEvent.KeepAlive(Clock.System.now())
            listeners.forEach {
                try {
                    it.emitter.emit(keepAliveEvent)
                } catch (_: Exception) {
                }
            }
        }
}
