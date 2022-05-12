package no.nav.syfo.sykmelding.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.post
import no.nav.syfo.application.HttpMessage
import no.nav.syfo.log
import no.nav.syfo.sykmelding.SlettSykmeldingService
import no.nav.syfo.sykmelding.SykmeldingService
import no.nav.syfo.sykmelding.model.SykmeldingRequest
import java.util.UUID

fun Route.registrerSykmeldingApi(sykmeldingService: SykmeldingService, slettSykmeldingService: SlettSykmeldingService) {
    post("/sykmelding/opprett") {
        val request = call.receive<SykmeldingRequest>()

        val mottakId = sykmeldingService.opprettSykmelding(request)

        log.info("Opprettet sykmelding")
        call.respond(HttpStatusCode.OK, HttpMessage("Opprettet sykmelding med mottakId $mottakId"))
    }

    delete("/sykmelding/{sykmeldingId}") {
        val sykmeldingId = call.parameters["sykmeldingId"]
        val fnr = call.request.headers["Sykmeldt-Fnr"]
        if (fnr == null || fnr.length != 11) {
            call.respond(HttpStatusCode.BadRequest, HttpMessage("Sykmeldt-Fnr mangler eller har feil lengde"))
            return@delete
        }
        try {
            UUID.fromString(sykmeldingId)
        } catch (e: Exception) {
            log.error("SykmeldingId er ikke en uuid: $sykmeldingId")
            call.respond(HttpStatusCode.BadRequest, HttpMessage("SykmeldingId må være en UUID"))
            return@delete
        }
        slettSykmeldingService.slettSykmelding(sykmeldingId = sykmeldingId!!, fnr = fnr)
        log.info("Slettet sykmelding med id $sykmeldingId")
        call.respond(HttpStatusCode.OK, HttpMessage("Slettet sykmelding med id $sykmeldingId"))
    }
}
