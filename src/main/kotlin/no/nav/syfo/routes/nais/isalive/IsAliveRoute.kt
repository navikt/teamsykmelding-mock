package no.nav.syfo.routes.nais.isalive

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import no.nav.syfo.ApplicationState

fun Routing.naisIsAliveRoute(
    applicationState: ApplicationState,
    alivenessCheck: () -> Boolean = { applicationState.alive },
) {
    get("/internal/is_alive") {
        if (alivenessCheck()) {
            call.respondText("I'm alive! :)")
        } else {
            call.respondText("I'm dead x_x", status = HttpStatusCode.InternalServerError)
        }
    }
}
