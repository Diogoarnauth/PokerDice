package pt.isel.daw.pokerDice

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

class TestClock(
    initialInstant: Instant = Instant.ofEpochSecond(0),
) : Clock() {
    private var testNow: Instant = initialInstant

    /** Avança o relógio em uma duração */
    fun advance(duration: Duration) {
        testNow = testNow.plus(duration)
    }

    /** Retorna o instante atual do relógio */
    override fun instant(): Instant = testNow

    /** Retorna o fuso horário do relógio */
    override fun getZone(): ZoneId = ZoneId.systemDefault()

    /** Retorna um relógio com o fuso horário especificado */
    override fun withZone(zone: ZoneId): Clock = this
}
