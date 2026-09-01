package no.nav.syfo.plugins

import io.ktor.serialization.jackson3.jackson
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.SerializationFeature

fun Application.configureContentNegotiation() {
    install(ContentNegotiation) {
        jackson {
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }
    }
}
