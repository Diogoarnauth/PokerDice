package pt.isel.daw.pokerDice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.time.Clock

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

annotation class Sha256TokenEncoder

@Bean
fun clock(): Clock = Clock.systemUTC()

@Bean
fun passwordEncoder() = BCryptPasswordEncoder()


@Bean
fun tokenEncoder() = Sha256TokenEncoder()
