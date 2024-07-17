package no.nav.syfo.routes.nais.isalive

import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import no.nav.syfo.plugins.ApplicationState
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class IsAliveRouteTest {

    @Test
    internal fun `Returns ok on is_alive`() {
        with(TestApplicationEngine()) {
            start()
            val applicationState = ApplicationState()
            applicationState.ready = true
            applicationState.alive = true
            application.routing { naisIsAliveRoute(applicationState) }

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
            application.routing { naisIsAliveRoute(applicationState) }

            with(handleRequest(HttpMethod.Get, "/is_alive")) {
                Assertions.assertEquals(HttpStatusCode.InternalServerError, response.status())
                Assertions.assertEquals("I'm dead x_x", response.content)
            }
        }
    }
}
