package no.nav.syfo.dolly.api

import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.syfo.dolly.DollyClient
import no.nav.syfo.dolly.model.DollyOpprettSykmeldingResponse
import no.nav.syfo.dolly.model.DollySykmelding
import no.nav.syfo.model.HttpMessage
import no.nav.syfo.utils.logger
import org.koin.ktor.ext.inject

fun Route.registrerDollySykmeldingApi() {
    val dollyClient: DollyClient by inject()

    post("/sykmelding") {
        val request = call.receive<DollySykmelding>()
        logger.info("DollySykmelding fra teamsykmelding-mock: $request")

        val opprettSykmelding: DollyOpprettSykmeldingResponse =
            dollyClient.opprettSykmelding(request)
        call.respond(opprettSykmelding.status, HttpMessage(opprettSykmelding.message))
    }
}
