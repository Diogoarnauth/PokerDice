package pt.isel.daw.pokerDice.domain

interface EventEmitter {
    fun emit(event: PokerEvent)

    fun onCompletion(callback: () -> Unit)

    fun onError(callback: (Throwable) -> Unit)

    fun complete()
}
