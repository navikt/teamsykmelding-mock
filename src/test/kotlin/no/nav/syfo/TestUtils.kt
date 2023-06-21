package no.nav.syfo

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.Route
import io.ktor.server.routing.routing
import io.ktor.server.testing.TestApplicationEngine
import no.nav.syfo.application.ApplicationState

fun withKtor(build: Route.() -> Unit, block: TestApplicationEngine.() -> Unit) {
    with(TestApplicationEngine()) {
        start()
        val applicationState = ApplicationState()
        applicationState.ready = true
        applicationState.alive = true
        application.install(ContentNegotiation) {
            jackson {
                registerKotlinModule()
                registerModule(JavaTimeModule())
                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
        application.routing { build() }

        block(this)
    }
}
