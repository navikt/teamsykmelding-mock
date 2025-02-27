package no.nav.syfo.routes.nais.isready

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import no.nav.syfo.plugins.ApplicationState
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class IsReadyRouteTest {
    @Test
    internal fun `Returns ok in is_ready`() {
        testApplication {
            application {
                val applicationState = ApplicationState()
                applicationState.ready = true
                applicationState.alive = true
                routing { naisIsReadyRoute(applicationState) }
            }

            val response = client.get("/is_ready")

            Assertions.assertEquals(HttpStatusCode.OK, response.status)
            Assertions.assertEquals("I'm ready! :)", response.bodyAsText())
        }
    }

    @Test
    internal fun `Returns internal server error when readyness check fails`() {
        testApplication {
            application {
                val applicationState = ApplicationState()
                applicationState.ready = false
                applicationState.alive = false
                routing { naisIsReadyRoute(applicationState) }
            }

            val response = client.get("/is_ready")
            Assertions.assertEquals(HttpStatusCode.InternalServerError, response.status)
            Assertions.assertEquals("Please wait! I'm not ready :(", response.bodyAsText())
        }
    }
}
