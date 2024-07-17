package no.nav.syfo.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import no.nav.syfo.metrics.monitorHttpRequests

fun Application.configureFeatures() {
    routing {
        route("/api") {
            // registerFnrApi()
        }
    }

    intercept(ApplicationCallPipeline.Monitoring, monitorHttpRequests())
}
