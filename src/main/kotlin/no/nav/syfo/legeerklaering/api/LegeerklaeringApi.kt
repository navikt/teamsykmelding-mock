package no.nav.syfo.legeerklaering.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.syfo.application.HttpMessage
import no.nav.syfo.legeerklaering.LegeerklaeringService
import no.nav.syfo.legeerklaering.model.LegeerklaeringRequest
import no.nav.syfo.log

fun Route.registrerLegeerklaeringApi(legeerklaeringService: LegeerklaeringService) {
    post("/legeerklaering/opprett") {
        val request = call.receive<LegeerklaeringRequest>()

        val mottakId = legeerklaeringService.opprettLegeerklaering(request)

        log.info("Opprettet legeerklæring")
        call.respond(HttpStatusCode.OK, HttpMessage("Opprettet legeerklæring med mottakId $mottakId"))
    }
}
