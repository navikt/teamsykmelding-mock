package no.nav.syfo.application.api

import io.ktor.server.http.content.defaultResource
import io.ktor.server.http.content.resources
import io.ktor.server.http.content.static
import io.ktor.server.routing.Route
import io.ktor.server.routing.route

fun Route.setupSwaggerDocApi() {
    route("/api/v1/docs/") {
        static {
            resources("api")
            defaultResource("api/index.html")
        }
    }
}
