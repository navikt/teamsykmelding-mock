package no.nav.syfo.sykmelding.api

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import no.nav.syfo.application.HttpMessage
import no.nav.syfo.log
import no.nav.syfo.sykmelding.SykmeldingService
import no.nav.syfo.sykmelding.model.SykmeldingRequest

fun Route.registrerSykmeldingApi(sykmeldingService: SykmeldingService) {
    post("/sykmelding/opprett") {
        val request = call.receive<SykmeldingRequest>()

        val mottakId = sykmeldingService.opprettSykmelding(request)

        log.info("Opprettet sykmelding")
        call.respond(HttpStatusCode.OK, HttpMessage("Opprettet sykmelding med mottakId $mottakId"))
    }
}
