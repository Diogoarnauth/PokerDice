package pt.isel.daw.pokerDice

import kotlinx.datetime.Clock
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
//import pt.isel.daw.pokerDice.repository.jdbi.configureWithAppRequirements // se tiveres esta função
import pt.isel.daw.pokerDice.domain.players.PlayersDomainConfig
import kotlin.time.Duration.Companion.hours

@SpringBootApplication
class PokerDiceApplication {

   /* @Bean
    fun jdbi(): Jdbi =
        Jdbi.create(
            PGSimpleDataSource().apply {
                setURL(System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/pokerdice?user=postgres&password=postgres")
            }
        ) // podes adicionar .configureWithAppRequirements() se existir
*/
    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun clock() = Clock.System

    @Bean
    fun playersDomainConfig() =
        PlayersDomainConfig(
            tokenSizeInBytes = 256 / 8,
            tokenTtl = 24.hours,
            tokenRollingTtl = 1.hours,
            maxTokensPerUser = 3,
        )
}

private val logger = LoggerFactory.getLogger("main")

fun main(args: Array<String>) {
    logger.info("Starting app")
    runApplication<PokerDiceApplication>(*args)
}
