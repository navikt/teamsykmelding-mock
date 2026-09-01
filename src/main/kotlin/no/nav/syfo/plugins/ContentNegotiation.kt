package no.nav.syfo.plugins

import io.ktor.serialization.jackson3.jackson
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.datatype.jsr310.JavaTimeModule

fun Application.configureContentNegotiation() {
    install(ContentNegotiation) {
        jackson {
            addModule(JavaTimeModule())
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
    }
}
