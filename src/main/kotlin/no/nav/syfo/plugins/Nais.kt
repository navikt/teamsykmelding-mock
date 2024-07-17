package no.nav.syfo.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import no.nav.syfo.no.nav.syfo.routes.nais.isready.naisPrometheusRoute
import no.nav.syfo.routes.nais.isalive.naisIsAliveRoute
import no.nav.syfo.routes.nais.isready.naisIsReadyRoute
import org.koin.ktor.ext.inject

fun Application.configureNaisResources() {
    val state by inject<ApplicationState>()
    routing {
        route("/api/internal") {
            naisIsAliveRoute(state)
            naisIsReadyRoute(state)
            naisPrometheusRoute()
        }
    }
}
