package no.nav.syfo.plugins

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*

fun Application.configureWebApp() {
    routing {
        singlePageApplication {
            useResources = true
            filesPath = "web-app"
            defaultPage = "index.html"
            ignoreFiles { it.endsWith(".txt") }
        }
    }
}
