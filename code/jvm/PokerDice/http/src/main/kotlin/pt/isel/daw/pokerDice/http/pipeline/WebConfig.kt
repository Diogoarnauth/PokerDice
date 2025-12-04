package pt.isel.daw.pokerDice.http.pipeline

import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val authenticationInterceptor: AuthenticationInterceptor,
    private val authenticatedUserArgumentResolver: AuthenticatedUserArgumentResolver,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        // Limita ao teu API se quiseres: .addInterceptor(...).addPathPatterns("/api/**")
        registry.addInterceptor(authenticationInterceptor)
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(authenticatedUserArgumentResolver)
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/api/**")
            .allowedOrigins("*") // aceita qualquer origem/porta
            .allowedMethods("GET", "POST", "OPTIONS", "DELETE")
            .allowedHeaders("Authorization", "Content-Type")
            .allowCredentials(true) // permite que as cookies sejam enviadas
    }
}
