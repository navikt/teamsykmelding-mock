package no.nav.syfo.papirsykmelding.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.syfo.application.HttpMessage
import no.nav.syfo.log
import no.nav.syfo.papirsykmelding.PapirsykmeldingService
import no.nav.syfo.papirsykmelding.model.PapirsykmeldingMappingException
import no.nav.syfo.papirsykmelding.model.PapirsykmeldingRequest

fun Route.registrerPapirsykmeldingApi(papirsykmeldingService: PapirsykmeldingService) {
    post("/papirsykmelding/opprett") {
        val request = call.receive<PapirsykmeldingRequest>()

        val journalpostId = papirsykmeldingService.opprettPapirsykmelding(request)

        log.info("Opprettet papirsykmelding med journalpostId $journalpostId")
        call.respond(
            HttpStatusCode.OK,
            HttpMessage("Opprettet papirsykmelding med journalpostId $journalpostId")
        )
    }

    post("/papirsykmelding/regelsjekk") {
        val request = call.receive<PapirsykmeldingRequest>()
        if (request.utenOcr) {
            call.respond(
                HttpStatusCode.BadRequest,
                HttpMessage("Kan ikke sjekke papirsykmelding uten OCR")
            )
            return@post
        }

        try {
            val validationResult = papirsykmeldingService.sjekkRegler(request)

            log.info("Har sjekket regler for papirsykmelding")
            call.respond(validationResult)
        } catch (e: PapirsykmeldingMappingException) {
            call.respond(
                HttpStatusCode.BadRequest,
                HttpMessage(e.message ?: "Kunne ikke mappe sykmelding til ReceivedSykmelding")
            )
        }
    }

    post("/papirsykmelding/utenlandsk/opprett") {
        val fnrSykmeldt = call.request.headers["Sykmeldt-Fnr"]

        if (fnrSykmeldt == null) {
            call.respond(HttpStatusCode.BadRequest, HttpMessage("Sykmeldt-Fnr mangler"))
            return@post
        }

        if (fnrSykmeldt.length != 11) {
            call.respond(
                HttpStatusCode.BadRequest,
                HttpMessage("Sykmeldt-Fnr har feil lengde, er ${fnrSykmeldt.length}")
            )
            return@post
        }

        val journalpostId = papirsykmeldingService.opprettUtenlandskPapirsykmelding(fnrSykmeldt)

        log.info("Opprettet utenlandsk papirsykmelding med journalpostId $journalpostId")
        call.respond(
            HttpStatusCode.OK,
            HttpMessage("Opprettet utenlandsk papirsykmelding med journalpostId $journalpostId")
        )
    }
}
