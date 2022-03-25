package no.nav.syfo.papirsykmelding.api

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import no.nav.syfo.application.HttpMessage
import no.nav.syfo.log
import no.nav.syfo.papirsykmelding.PapirsykmeldingService
import no.nav.syfo.papirsykmelding.model.PapirsykmeldingRequest

fun Route.registrerPapirsykmeldingApi(papirsykmeldingService: PapirsykmeldingService) {
    post("/papirsykmelding/opprett") {
        val request = call.receive<PapirsykmeldingRequest>()

        val journalpostId = papirsykmeldingService.opprettPapirsykmelding(request)

        log.info("Opprettet papirsykmelding med journalpostId $journalpostId")
        call.respond(HttpStatusCode.OK, HttpMessage("Opprettet papirsykmelding med journalpostId $journalpostId"))
    }
}
