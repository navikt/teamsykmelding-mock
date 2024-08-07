package no.nav.syfo.legeerklaering.api

import io.ktor.http.*
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.syfo.legeerklaering.LegeerklaeringService
import no.nav.syfo.legeerklaering.model.LegeerklaeringRequest
import no.nav.syfo.model.HttpMessage
import no.nav.syfo.utils.logger
import org.koin.ktor.ext.inject

fun Route.registrerLegeerklaeringApi() {
    val legeerklaeringService by inject<LegeerklaeringService>()
    post("/legeerklaering/opprett") {
        logger.info("revice request to create legeerklaering")
        val request = call.receive<LegeerklaeringRequest>()

        val mottakId = legeerklaeringService.opprettLegeerklaering(request)

        logger.info("Opprettet legeerklæring")
        call.respond(
            HttpStatusCode.OK,
            HttpMessage("Opprettet legeerklæring med mottakId $mottakId")
        )
    }
}
