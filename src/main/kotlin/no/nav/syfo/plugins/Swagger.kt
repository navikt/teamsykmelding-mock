package no.nav.syfo.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import no.nav.syfo.utils.EnvironmentVariables
import org.koin.ktor.ext.inject

fun Application.configureSwagger() {
    val env by inject<EnvironmentVariables>()
    routing { swaggerUI(path = "docs", swaggerFile = "openapi/documentation.yaml") }
}
