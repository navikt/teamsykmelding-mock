package no.nav.syfo

import configureKoin
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.syfo.plugins.*
import no.nav.syfo.utils.getEnvVar
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger = LoggerFactory.getLogger("no.nav.syfo.teamsykmelding-mock-backend")
val securelog: Logger = LoggerFactory.getLogger("securelog")

fun main() {
    embeddedServer(
            Netty,
            port = getEnvVar("APPLICATION_PORT", "8080").toInt(),
            module = Application::module,
        )
        .start(true)
}

fun Application.module() {
    configureKoin()
    configureContentNegotiation()
    configureAuth()
    configurePrometheus()
    configureSwagger()
    configureLifecycleHooks()
    configureWebApp()
    configureNaisResources()
    configureFeatures()
}
