package pt.isel.daw.pokerDice.http.pipeline

import pt.isel.daw.pokerDice.domain.players.AuthenticatedPlayer
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class AuthenticatedPlayerArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter) = parameter.parameterType == AuthenticatedPlayer::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any? {
        val request =
            webRequest.getNativeRequest(HttpServletRequest::class.java)
                ?: throw IllegalStateException("TODO")
        return getPlayerFrom(request) ?: throw IllegalStateException("TODO")
    }

    companion object {
        private const val KEY = "AuthenticatedPlayerArgumentResolver"

        fun addPlayerTo(
            player: AuthenticatedPlayer,
            request: HttpServletRequest,
        ) {
            return request.setAttribute(KEY, player)
        }

        fun getPlayerFrom(request: HttpServletRequest): AuthenticatedPlayer? {
            return request.getAttribute(KEY)?.let {
                it as? AuthenticatedPlayer
            }
        }
    }
}
