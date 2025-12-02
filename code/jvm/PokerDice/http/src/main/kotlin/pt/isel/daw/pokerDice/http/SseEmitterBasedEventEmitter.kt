package pt.isel.daw.pokerDice.events

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import pt.isel.daw.pokerDice.domain.EventEmitter
import pt.isel.daw.pokerDice.domain.PokerEvent

class SseEmitterBasedEventEmitter(
    private val emitter: SseEmitter,
) : EventEmitter {
    override fun emit(event: PokerEvent) {
        val sse =
            SseEmitter
                .event()
                .name(event.type)
                .data(event)

        emitter.send(sse)
    }

    override fun onCompletion(callback: () -> Unit) {
        emitter.onCompletion(callback)
    }

    override fun onError(callback: (Throwable) -> Unit) {
        emitter.onError(callback)
    }

    override fun complete() {
        emitter.complete()
    }
}
