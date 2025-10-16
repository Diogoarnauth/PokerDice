package pt.isel.daw.pokerDice

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

class TestClock(
    private var currentInstant: Instant = Instant.ofEpochSecond(0),
) : Clock() {
    override fun instant(): Instant = currentInstant

    override fun getZone(): ZoneId = ZoneId.systemDefault()

    override fun withZone(zone: ZoneId): Clock = this // mantém a mesma implementação para testes

    fun advance(duration: Duration) {
        currentInstant = currentInstant.plus(duration)
    }
}
