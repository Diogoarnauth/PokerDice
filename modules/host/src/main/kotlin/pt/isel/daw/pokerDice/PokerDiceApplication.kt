package pt.isel.daw.pokerDice

import kotlinx.datetime.Clock
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import pt.isel.daw.pokerDice.domain.invite.InviteDomainConfig
import pt.isel.daw.pokerDice.domain.invite.Sha256InviteEncoder
import pt.isel.daw.pokerDice.domain.lobbies.LobbiesDomainConfig
import pt.isel.daw.pokerDice.domain.users.Sha256TokenEncoder
import pt.isel.daw.pokerDice.domain.users.UsersDomainConfig
import pt.isel.daw.pokerDice.http.pipeline.AuthenticatedUserArgumentResolver
import pt.isel.daw.pokerDice.http.pipeline.AuthenticationInterceptor
import pt.isel.daw.pokerDice.repository.jdbi.configureWithAppRequirements
import kotlin.time.Duration.Companion.hours

@SpringBootApplication
class PokerDiceApplication {
    @Bean
    fun jdbi() =
        Jdbi
            .create(
                PGSimpleDataSource().apply {
                    setURL(Environment.getDbUrl())
                },
            ).configureWithAppRequirements()

    @Bean
    fun tokenEncoder() = Sha256TokenEncoder()

    @Bean
    fun inviteEncoder() = Sha256InviteEncoder()

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun clock() = Clock.System

    @Bean
    fun usersDomainConfig() =
        UsersDomainConfig(
            tokenSizeInBytes = 256 / 8,
            tokenTtl = 24.hours,
            tokenRollingTtl = 1.hours,
            maxTokensPerUser = 3,
        )

    @Bean
    fun inviteDomainConfig() =
        InviteDomainConfig(
            validState = "pending",
            expireInviteTime = 24.hours,
            expiredState = "expired",
            usedState = "used",
            declinedState = "declined",
        )

    @Bean
    fun lobbiesDomainConfig() =
        LobbiesDomainConfig(
            minUsersAllowed = 2,
            maxUsersAllowed = 5,
            minRoundsAllowed = 2,
            maxRoundsAllowed = 10,
            minCreditAllowed = 20,
        )
}

@Configuration
class PipelineConfigurer(
    val authenticationInterceptor: AuthenticationInterceptor,
    val authenticatedUserArgumentResolver: AuthenticatedUserArgumentResolver,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authenticationInterceptor)
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(authenticatedUserArgumentResolver)
    }
}

private val logger = LoggerFactory.getLogger("main")

fun main(args: Array<String>) {
    logger.info("Starting app")
    runApplication<PokerDiceApplication>(*args)
}
