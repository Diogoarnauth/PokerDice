package pt.isel.daw.pokerDice.http.pipeline

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType

object ProblemWriter {
    fun writeJson(
        response: HttpServletResponse,
        status: Int,
        body: Any,
        mapper: ObjectMapper,
    ) {
        response.status = status
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.writer.use { it.write(mapper.writeValueAsString(body)) }
    }
}
