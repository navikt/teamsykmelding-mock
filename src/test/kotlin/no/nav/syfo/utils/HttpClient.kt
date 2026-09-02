package no.nav.syfo.utils

import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson3.jackson
import io.ktor.server.testing.*

fun ApplicationTestBuilder.testClient() = createClient {
    install(ContentNegotiation) { jackson {} }
}
