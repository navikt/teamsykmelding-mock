package no.nav.syfo.papirsykmelding.api

import io.ktor.http.*
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.syfo.model.HttpMessage
import no.nav.syfo.model.JournalpostOpprettetResponse
import no.nav.syfo.model.Status
import no.nav.syfo.papirsykmelding.PapirsykmeldingService
import no.nav.syfo.papirsykmelding.model.PapirsykmeldingMappingException
import no.nav.syfo.papirsykmelding.model.PapirsykmeldingRequest
import no.nav.syfo.papirsykmelding.model.UtenlandskPapirsykmeldingRequest
import no.nav.syfo.utils.logger
import org.koin.ktor.ext.inject

fun Route.registrerPapirsykmeldingApi() {
    val papirsykmeldingService by inject<PapirsykmeldingService>()

    post("/papirsykmelding/opprett") {
        val request = call.receive<PapirsykmeldingRequest>()

        val isRuleOK: Boolean =
            if (request.utenOcr) false
            else
                try {
                    val validationResult = papirsykmeldingService.sjekkRegler(request)
                    validationResult.status == Status.OK
                } catch (_: Exception) {
                    false
                }

        val createdJournalPostId: String = papirsykmeldingService.opprettPapirsykmelding(request)

        logger.info(
            "Opprettet papirsykmelding med journalpostId ${createdJournalPostId}",
        )
        call.respond(
            HttpStatusCode.OK,
            JournalpostOpprettetResponse(
                status = "OK",
                message = "Opprettet papirsykmelding med journalpostId ${createdJournalPostId}",
                journalpostID = createdJournalPostId,
                automatic = isRuleOK,
            ),
        )
    }

    post("/papirsykmelding/regelsjekk") {
        val request = call.receive<PapirsykmeldingRequest>()
        if (request.utenOcr) {
            call.respond(
                HttpStatusCode.BadRequest,
                HttpMessage("Kan ikke sjekke papirsykmelding uten OCR"),
            )
            return@post
        }

        try {
            val validationResult = papirsykmeldingService.sjekkRegler(request)

            logger.info("Har sjekket regler for papirsykmelding")
            call.respond(validationResult)
        } catch (e: PapirsykmeldingMappingException) {
            call.respond(
                HttpStatusCode.BadRequest,
                HttpMessage(e.message ?: "Kunne ikke mappe sykmelding til ReceivedSykmelding"),
            )
        }
    }

    post("/papirsykmelding/utenlandsk/opprett") {
        val fnrSykmeldt = call.request.headers["Sykmeldt-Fnr"]
        if (fnrSykmeldt != null && fnrSykmeldt.length != 11) {
            call.respond(
                HttpStatusCode.BadRequest,
                HttpMessage("Sykmeldt-Fnr har feil lengde, er ${fnrSykmeldt.length}"),
            )
            return@post
        }
        logger.info("utenlandsk papirsykmelding med fnr fra header: $fnrSykmeldt")
        val request = call.receive<UtenlandskPapirsykmeldingRequest>()
        logger.info("utenlandsk papirsykmelding med fnr fra requestclas: ${request.fnr}")
        val journalpostId = papirsykmeldingService.opprettUtenlandskPapirsykmelding(request.fnr)
        logger.info("Opprettet utenlandsk papirsykmelding med journalpostId $journalpostId")
        call.respond(
            HttpStatusCode.OK,
            JournalpostOpprettetResponse(
                "OK",
                "Opprettet utenlandsk papirsykmelding med journalpostId $journalpostId",
                journalpostId,
            ),
        )
    }
}
