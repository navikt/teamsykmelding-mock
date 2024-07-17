package no.nav.syfo.routes.nais.isready

import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import no.nav.syfo.plugins.ApplicationState
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class IsReadyRouteTest {
    @Test
    internal fun `Returns ok in is_ready`() {
        with(TestApplicationEngine()) {
            start()
            val applicationState = ApplicationState()
            applicationState.ready = true
            applicationState.alive = true
            application.routing { naisIsReadyRoute(applicationState) }

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
            application.routing { naisIsReadyRoute(applicationState) }

            with(handleRequest(HttpMethod.Get, "/is_ready")) {
                Assertions.assertEquals(HttpStatusCode.InternalServerError, response.status())
                Assertions.assertEquals("Please wait! I'm not ready :(", response.content)
            }
        }
    }
}
