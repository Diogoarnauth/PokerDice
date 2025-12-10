package pt.isel.daw.pokerDice.http.pipeline

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import pt.isel.daw.pokerDice.domain.users.AuthenticatedUser
import pt.isel.daw.pokerDice.http.model.Problem

@Component
class AuthenticationInterceptor(
    private val authorizationHeaderProcessor: RequestTokenProcessor,
    private val objectMapper: ObjectMapper,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (handler is HandlerMethod &&
            handler.methodParameters.any {
                it.parameterType == AuthenticatedUser::class.java
            }
        ) {
            val cookies = request.cookies ?: emptyArray()

            if (request.method == "OPTIONS") return true

            val userFromAuthHeader =
                authorizationHeaderProcessor.processAuthorizationHeaderValue(
                    request.getHeader(NAME_AUTHORIZATION_HEADER),
                )

            val userFromCookie = authorizationHeaderProcessor.processCookieToken(cookies)

            val user = userFromAuthHeader ?: userFromCookie

            if (user == null) {
                logger.info("Authentication failed for ${request.requestURI}")
                response.addHeader(NAME_WWW_AUTHENTICATE_HEADER, RequestTokenProcessor.SCHEME)
                val problem =
                    mapOf(
                        "error" to
                            mapOf(
                                "type" to Problem.tokenInvalid.type,
                                "title" to "Unauthorized",
                                "detail" to "Missing or invalid bearer token.",
                            ),
                    )
                ProblemWriter.writeJson(
                    response = response,
                    status = HttpServletResponse.SC_UNAUTHORIZED,
                    body = problem,
                    mapper = objectMapper,
                )
                return false
            } else {
                AuthenticatedUserArgumentResolver.addUserTo(user, request)
                true
            }
        }
        return true
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuthenticationInterceptor::class.java)
        const val NAME_AUTHORIZATION_HEADER = "Authorization"
        private const val NAME_WWW_AUTHENTICATE_HEADER = "WWW-Authenticate"
    }
}
