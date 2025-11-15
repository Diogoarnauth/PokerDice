package pt.isel.daw.pokerDice.http

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import pt.isel.daw.pokerDice.domain.Event
import pt.isel.daw.pokerDice.domain.EventEmitter

// - SseEmitter - Spring MVC type
// - EventEmitter is our own type (domain)
// - SseEmitterBasedEventEmitter is our own type (http),
//   which uses SseEmitter

class SseEmitterBasedEventEmitter( // TODO()
    private val sseEmitter: SseEmitter,
) : EventEmitter {
    override fun emit(event: Event) {
        val event =
            when (event) {
                is Event.Message ->
                    SseEmitter
                        .event()
                        .id(event.id.toString())
                        .name("message")
                        .data(event)

                is Event.KeepAlive ->
                    SseEmitter
                        .event()
                        .comment(event.timestamp.epochSeconds.toString())
            }
        sseEmitter.send(event)
    }

    override fun onCompletion(callback: () -> Unit) {
        sseEmitter.onCompletion(callback)
    }

    override fun onError(callback: (Throwable) -> Unit) {
        sseEmitter.onError(callback)
    }
}
