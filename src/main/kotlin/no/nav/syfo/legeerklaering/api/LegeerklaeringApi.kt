package no.nav.syfo.legeerklaering.api

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import no.nav.syfo.application.HttpMessage
import no.nav.syfo.legeerklaering.LegeerklaeringService
import no.nav.syfo.legeerklaering.model.LegeerklaeringRequest
import no.nav.syfo.log

fun Route.registrerLegeerklaeringApi(legeerklaeringService: LegeerklaeringService) {
    post("/legeerklaering/opprett") {
        val request = call.receive<LegeerklaeringRequest>()

        legeerklaeringService.opprettLegeerklaering(request)

        log.info("Opprettet legeerklæring")
        call.respond(HttpStatusCode.OK, HttpMessage("Opprettet legeerklæring"))
    }
}
