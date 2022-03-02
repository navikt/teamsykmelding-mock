package no.nav.syfo.application.api

import io.ktor.http.content.defaultResource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.routing.Route
import io.ktor.routing.route

fun Route.setupSwaggerDocApi() {
    route("/api/v1/docs/") {
        static {
            resources("api")
            defaultResource("api/index.html")
        }
    }
}
