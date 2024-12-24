package no.nav.syfo.routes.nais.isalive

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import no.nav.syfo.plugins.ApplicationState
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class IsAliveRouteTest {

    @Test
    internal fun `Returns ok on is_alive`() {
        testApplication {
            application {
                val applicationState = ApplicationState()
                applicationState.ready = true
                applicationState.alive = true
                routing { naisIsAliveRoute(applicationState) }
            }
            val response = client.get("/is_alive")

            Assertions.assertEquals(HttpStatusCode.OK, response.status)
            Assertions.assertEquals("I'm alive! :)", response.bodyAsText())
        }
    }

    @Test
    internal fun `Returns internal server error when liveness check fails`() {
        testApplication {
            application {
                val applicationState = ApplicationState()
                applicationState.ready = false
                applicationState.alive = false
                routing { naisIsAliveRoute(applicationState) }
            }
            val response = client.get("/is_alive")

            Assertions.assertEquals(HttpStatusCode.InternalServerError, response.status)
            Assertions.assertEquals("I'm dead x_x", response.bodyAsText())
        }
    }
}
