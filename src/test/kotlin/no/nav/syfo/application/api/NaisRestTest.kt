package no.nav.syfo.application.api

import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import no.nav.syfo.application.ApplicationState
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class NaisRestTest {

    @Test
    internal fun `Returns ok on is_alive`() {
        with(TestApplicationEngine()) {
            start()
            val applicationState = ApplicationState()
            applicationState.ready = true
            applicationState.alive = true
            application.routing { registerNaisApi(applicationState) }

            with(handleRequest(HttpMethod.Get, "/is_alive")) {
                Assertions.assertEquals(HttpStatusCode.OK, response.status())
                Assertions.assertEquals("I'm alive! :)", response.content)
            }
        }
    }

    @Test
    internal fun `Returns internal server error when liveness check fails`() {
        with(TestApplicationEngine()) {
            start()
            val applicationState = ApplicationState()
            applicationState.ready = false
            applicationState.alive = false
            application.routing { registerNaisApi(applicationState) }

            with(handleRequest(HttpMethod.Get, "/is_alive")) {
                Assertions.assertEquals(HttpStatusCode.InternalServerError, response.status())
                Assertions.assertEquals("I'm dead x_x", response.content)
            }
        }
    }

    @Test
    internal fun `Returns ok in is_ready`() {
        with(TestApplicationEngine()) {
            start()
            val applicationState = ApplicationState()
            applicationState.ready = true
            applicationState.alive = true
            application.routing { registerNaisApi(applicationState) }

            with(handleRequest(HttpMethod.Get, "/is_ready")) {
                Assertions.assertEquals(HttpStatusCode.OK, response.status())
                Assertions.assertEquals("I'm ready! :)", response.content)
            }
        }
    }

    @Test
    internal fun `Returns internal server error when readyness check fails`() {
        with(TestApplicationEngine()) {
            start()
            val applicationState = ApplicationState()
            applicationState.ready = false
            applicationState.alive = false
            application.routing { registerNaisApi(applicationState) }

            with(handleRequest(HttpMethod.Get, "/is_ready")) {
                Assertions.assertEquals(HttpStatusCode.InternalServerError, response.status())
                Assertions.assertEquals("Please wait! I'm not ready :(", response.content)
            }
        }
    }
}
