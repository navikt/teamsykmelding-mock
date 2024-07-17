package no.nav.syfo.plugins

import io.ktor.server.application.*
import io.prometheus.client.hotspot.DefaultExports

fun Application.configurePrometheus() {
    DefaultExports.initialize()
}
